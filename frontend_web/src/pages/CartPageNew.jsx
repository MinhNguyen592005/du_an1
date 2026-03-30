import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { cartAPI } from "../services/cartApi";
import "./CartPageNew.css";

function CartPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [cart, setCart] = useState(null);
  const [error, setError] = useState("");
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    document.title = "Giỏ hàng - E-Commerce";
    loadCart();
  }, []);

  const loadCart = async () => {
    try {
      setLoading(true);
      setError("");
      console.log("=== Loading cart ===");
      const response = await cartAPI.getCart();

      console.log("Cart response:", response);

      if (response.code === 1000) {
        console.log("Cart data:", response.result);

        // Check if cartItems is null - backend bug
        if (
          response.result.cartItems === null &&
          response.result.totalItems > 0
        ) {
          console.error(
            "Backend bug: cartItems is null but totalItems is",
            response.result.totalItems,
          );
          setError(
            "⚠️ Backend Error: Cart items không được trả về từ API. Backend cần sửa CartService.getCartByUserId() để populate cartItems vào response!",
          );
        }

        setCart(response.result);
      } else {
        console.error("Cart error:", response);
        setError(response.message || "Không thể tải giỏ hàng");
      }
    } catch (err) {
      console.error("Error loading cart:", err);
      setError("Vui lòng đăng nhập để xem giỏ hàng");
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateQuantity = async (itemId, newQuantity) => {
    if (newQuantity < 1) return;

    try {
      setUpdating(true);
      const response = await cartAPI.updateCartItem(itemId, newQuantity);

      if (response.code === 1000) {
        setCart(response.result);
      } else {
        alert(response.message || "Không thể cập nhật số lượng");
      }
    } catch (err) {
      console.error("Error updating quantity:", err);
      alert("Có lỗi xảy ra khi cập nhật số lượng");
    } finally {
      setUpdating(false);
    }
  };

  const handleRemoveItem = async (itemId) => {
    if (!window.confirm("Bạn có chắc muốn xóa sản phẩm này?")) return;

    try {
      setUpdating(true);
      const response = await cartAPI.removeFromCart(itemId);

      if (response.code === 1000) {
        setCart(response.result);
      } else {
        alert(response.message || "Không thể xóa sản phẩm");
      }
    } catch (err) {
      console.error("Error removing item:", err);
      alert("Có lỗi xảy ra khi xóa sản phẩm");
    } finally {
      setUpdating(false);
    }
  };

  const handleClearCart = async () => {
    if (!window.confirm("Bạn có chắc muốn xóa toàn bộ giỏ hàng?")) return;

    try {
      setUpdating(true);
      const response = await cartAPI.clearCart();

      if (response.code === 1000) {
        loadCart();
      } else {
        alert(response.message || "Không thể xóa giỏ hàng");
      }
    } catch (err) {
      console.error("Error clearing cart:", err);
      alert("Có lỗi xảy ra");
    } finally {
      setUpdating(false);
    }
  };

  const handleCheckout = () => {
    if (!cart || cart.cartItems.length === 0) {
      alert("Giỏ hàng trống");
      return;
    }
    navigate("/checkout");
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(value || 0);
  };

  if (loading) {
    return (
      <div className="cart-page">
        <div className="loading">
          <div className="spinner"></div>
          <p>Đang tải giỏ hàng...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="cart-page">
        <div className="error-message">
          <h2>⚠️ {error}</h2>
          <Link to="/login">
            <button className="btn-primary">Đăng nhập</button>
          </Link>
        </div>
      </div>
    );
  }

  const isEmpty = !cart || !cart.cartItems || cart.cartItems.length === 0;

  return (
    <div className="cart-page">
      {error && <div className="alert alert-error">{error}</div>}

      {isEmpty ? (
        <div className="empty-cart">
          <h2>Giỏ hàng trống</h2>
          <p>Hãy thêm sản phẩm vào giỏ hàng của bạn</p>
          <Link to="/">
            <button className="btn-primary">Khám phá sản phẩm</button>
          </Link>
        </div>
      ) : (
        <div className="cart-content">
          {/* Cart Items */}
          <div className="cart-items-section">
            <div className="items-header">
              <h3>Sản phẩm ({cart.totalItems})</h3>
              <button
                className="btn-clear-cart"
                onClick={handleClearCart}
                disabled={updating}
              >
                Xóa tất cả
              </button>
            </div>

            <div className="cart-items-list">
              {cart.cartItems.map((item) => (
                <div key={item.id} className="cart-item">
                  <div className="item-left">
                    <div className="item-image">
                      {item.productImage ? (
                        <img src={item.productImage} alt={item.productName} />
                      ) : (
                        <div className="image-placeholder">📦</div>
                      )}
                    </div>

                    <div className="item-info">
                      <h3>{item.productName}</h3>
                      <p className="item-price">
                        {formatCurrency(item.productPrice)}
                      </p>
                    </div>
                  </div>

                  <div className="item-right">
                    <div className="item-quantity">
                      <button
                        onClick={() =>
                          handleUpdateQuantity(item.id, item.quantity - 1)
                        }
                        className="qty-btn"
                        disabled={updating || item.quantity <= 1}
                      >
                        −
                      </button>
                      <span className="qty-value">{item.quantity}</span>
                      <button
                        onClick={() =>
                          handleUpdateQuantity(item.id, item.quantity + 1)
                        }
                        className="qty-btn"
                        disabled={updating}
                      >
                        +
                      </button>
                    </div>

                    <div className="item-total">
                      <p className="total-price">
                        {formatCurrency(item.totalPrice)}
                      </p>
                    </div>

                    <button
                      onClick={() => handleRemoveItem(item.id)}
                      className="btn-remove"
                      disabled={updating}
                      title="Xóa sản phẩm"
                    >
                      Xóa
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Cart Summary */}
          <div className="cart-summary">
            <h3>Tổng đơn hàng</h3>

            <div className="summary-row">
              <span>Tạm tính:</span>
              <span>{formatCurrency(cart.totalPrice)}</span>
            </div>

            <div className="summary-row">
              <span>Số lượng:</span>
              <span>{cart.totalItems} sản phẩm</span>
            </div>

            <div className="summary-divider"></div>

            <div className="summary-row total">
              <span>Tổng cộng:</span>
              <span className="total-amount">
                {formatCurrency(cart.totalPrice)}
              </span>
            </div>

            <button
              onClick={handleCheckout}
              className="btn-checkout"
              disabled={updating}
            >
              Tiến hành thanh toán →
            </button>

            <p className="note">Phí vận chuyển sẽ được tính ở bước tiếp theo</p>
          </div>
        </div>
      )}
    </div>
  );
}

export default CartPage;
