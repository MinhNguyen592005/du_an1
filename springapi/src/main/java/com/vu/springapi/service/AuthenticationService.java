package com.vu.springapi.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.vu.springapi.dto.request.AuthenticationRequest;
import com.vu.springapi.dto.request.ForgetPasswordRequest;
import com.vu.springapi.dto.request.IntrospectRequest;
import com.vu.springapi.dto.request.LogoutRequest;
import com.vu.springapi.dto.request.ResetPasswordRequest;
import com.vu.springapi.dto.request.VerifyOtpRequest;
import com.vu.springapi.dto.response.AuthenticationResponse;
import com.vu.springapi.dto.response.ForgetPasswordResponse;
import com.vu.springapi.dto.response.IntrospectResponse;
import com.vu.springapi.dto.response.ResetPasswordResponse;
import com.vu.springapi.dto.response.VerifyOtpResponse;
import com.vu.springapi.exception.AppException;
import com.vu.springapi.exception.ErrorCode;
import com.vu.springapi.model.InvalidatedToken;
import com.vu.springapi.model.OtpToken;
import com.vu.springapi.model.User;
import com.vu.springapi.repository.InvalidatedTokenRepository;
import com.vu.springapi.repository.OtpTokenRepository;
import com.vu.springapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        var user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new AppException(ErrorCode.USERNAME_INCORRECT));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated){
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }
        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signToken = verifyToken(request.getToken());

        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
    }


    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public ForgetPasswordResponse forgetPassword (ForgetPasswordRequest request){
        if(!userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.EMAIL_NOT_FOUND);
        }

        String otp = generateOtp();

        OtpToken otpToken = OtpToken.builder()
                .email(request.getEmail())
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        otpTokenRepository.save(otpToken);

        emailService.sendOtpEmail(request.getEmail(), otp);

        return ForgetPasswordResponse.builder()
                .exist(true)
                .build();
    }

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        OtpToken otpToken = otpTokenRepository
                .findByEmailAndOtpAndUsedFalseAndExpiryTimeAfter(
                        request.getEmail(),
                        request.getOtp(),
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new AppException(ErrorCode.OTP_INVALID));

        return VerifyOtpResponse.builder()
                .valid(true)
                .build();
    }

    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        OtpToken otpToken = otpTokenRepository
                .findByEmailAndOtpAndUsedFalseAndExpiryTimeAfter(
                        request.getEmail(),
                        request.getOtp(),
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new AppException(ErrorCode.OTP_INVALID));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        return ResetPasswordResponse.builder()
                .success(true)
                .build();
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository
                .existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private String generateToken(User user){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("vu.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header,payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token");
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if(!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });
        }
        return stringJoiner.toString();
    }
}
