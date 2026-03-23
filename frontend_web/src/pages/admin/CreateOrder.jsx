import React, { useState, useEffect } from "react";
import { useProducts } from "../../context/ProductContext";
import "./CreateOrder.css";

const CreateOrder = () => {
  const { products, updateProduct } = useProducts();

  // State cho thông tin khách hàng
  const [customerInfo, setCustomerInfo] = useState({
    phone: "",
    name: "",
    address: "",
    city: "",
    district: "",
    ward: "",
    notesForPrint: "",
    notesInternal: "",
  });

  // State cho sản phẩm
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedItems, setSelectedItems] = useState([]);
  const [showProductList, setShowProductList] = useState(false);
  const [isBanLe, setIsBanLe] = useState(true);
  const [showStockModal, setShowStockModal] = useState(false);

  // State cho thanh toán
  const [discountPercent, setDiscountPercent] = useState(0);
  const [paymentMethod, setPaymentMethod] = useState("cash");
  const [amountPaid, setAmountPaid] = useState(0);

  // State cho địa chỉ Việt Nam
  const [provinces, setProvinces] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [wards, setWards] = useState([]);

  // Load dữ liệu tỉnh/thành phố từ API
  useEffect(() => {
    fetch("https://provinces.open-api.vn/api/p/")
      .then((res) => res.json())
      .then((data) => setProvinces(data))
      .catch((err) => console.error("Error loading provinces:", err));
  }, []);

  // Load quận/huyện khi chọn tỉnh/thành phố
  useEffect(() => {
    if (customerInfo.city) {
      const province = provinces.find((p) => p.name === customerInfo.city);
      if (province) {
        fetch(`https://provinces.open-api.vn/api/p/${province.code}?depth=2`)
          .then((res) => res.json())
          .then((data) => {
            setDistricts(data.districts || []);
            setCustomerInfo((prev) => ({ ...prev, district: "", ward: "" }));
          })
          .catch((err) => console.error("Error loading districts:", err));
      }
    }
  }, [customerInfo.city, provinces]);

  // Load phường/xã khi chọn quận/huyện
  useEffect(() => {
    if (customerInfo.district) {
      const district = districts.find((d) => d.name === customerInfo.district);
      if (district) {
        fetch(`https://provinces.open-api.vn/api/d/${district.code}?depth=2`)
          .then((res) => res.json())
          .then((data) => {
            setWards(data.wards || []);
            setCustomerInfo((prev) => ({ ...prev, ward: "" }));
          })
          .catch((err) => console.error("Error loading wards:", err));
      }
    }
  }, [customerInfo.district, districts]);

  // Xử lý tìm kiếm sản phẩm
  const filteredProducts = products.filter((product) =>
    product.name.toLowerCase().includes(searchTerm.toLowerCase()),
  );

  // Xử lý phím tắt F3
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "F3") {
        e.preventDefault();
        document.getElementById("product-search")?.focus();
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  // Thêm sản phẩm vào đơn hàng
  const handleAddProduct = (product) => {
    if (
      !product.colors ||
      product.colors.length === 0 ||
      !product.sizes ||
      product.sizes.length === 0
    ) {
      alert("Sản phẩm chưa có màu sắc hoặc kích thước!");
      return;
    }

    const newItem = {
      productId: product.id,
      name: product.name,
      image: product.images?.[0] || "",
      color: product.colors[0],
      size: product.sizes[0],
      price: isBanLe ? product.price : product.costPrice || product.price * 0.7,
      quantity: 1,
      subtotal: isBanLe
        ? product.price
        : product.costPrice || product.price * 0.7,
      variantKey: `${product.colors[0]}-${product.sizes[0]}`,
      product: product,
    };

    // Kiểm tra tồn kho
    const availableStock = product.variantStock?.[newItem.variantKey] || 0;
    if (availableStock <= 0) {
      alert("Sản phẩm này đã hết hàng với màu sắc và kích thước được chọn!");
      return;
    }

    setSelectedItems([...selectedItems, { ...newItem, id: Date.now() }]);
    setSearchTerm("");
    setShowProductList(false);

    // Trừ số lượng tồn kho
    updateStockQuantity(product, newItem.variantKey, -1);
  };

  // Cập nhật số lượng tồn kho
  const updateStockQuantity = (product, variantKey, change) => {
    const currentStock = product.variantStock?.[variantKey] || 0;
    const newStock = currentStock + change;

    if (newStock < 0) {
      alert("Không đủ số lượng tồn kho!");
      return false;
    }

    const updatedVariantStock = {
      ...product.variantStock,
      [variantKey]: newStock,
    };

    const totalStock = Object.values(updatedVariantStock).reduce(
      (sum, qty) => sum + qty,
      0,
    );

    updateProduct(product.id, {
      variantStock: updatedVariantStock,
      stock: totalStock,
    });

    return true;
  };

  // Thay đổi màu sắc của sản phẩm trong đơn
  const handleColorChange = (itemId, newColor) => {
    setSelectedItems((prevItems) =>
      prevItems.map((item) => {
        if (item.id === itemId) {
          const oldVariantKey = item.variantKey;
          const newVariantKey = `${newColor}-${item.size}`;

          // Hoàn lại tồn kho cũ
          updateStockQuantity(item.product, oldVariantKey, item.quantity);

          // Kiểm tra và trừ tồn kho mới
          const availableStock =
            item.product.variantStock?.[newVariantKey] || 0;
          if (availableStock < item.quantity) {
            alert("Không đủ số lượng tồn kho cho màu sắc này!");
            return item;
          }

          updateStockQuantity(item.product, newVariantKey, -item.quantity);

          return {
            ...item,
            color: newColor,
            variantKey: newVariantKey,
          };
        }
        return item;
      }),
    );
  };

  // Thay đổi kích thước của sản phẩm trong đơn
  const handleSizeChange = (itemId, newSize) => {
    setSelectedItems((prevItems) =>
      prevItems.map((item) => {
        if (item.id === itemId) {
          const oldVariantKey = item.variantKey;
          const newVariantKey = `${item.color}-${newSize}`;

          // Hoàn lại tồn kho cũ
          updateStockQuantity(item.product, oldVariantKey, item.quantity);

          // Kiểm tra và trừ tồn kho mới
          const availableStock =
            item.product.variantStock?.[newVariantKey] || 0;
          if (availableStock < item.quantity) {
            alert("Không đủ số lượng tồn kho cho kích thước này!");
            return item;
          }

          updateStockQuantity(item.product, newVariantKey, -item.quantity);

          return {
            ...item,
            size: newSize,
            variantKey: newVariantKey,
          };
        }
        return item;
      }),
    );
  };

  // Thay đổi số lượng sản phẩm
  const handleQuantityChange = (itemId, newQuantity) => {
    if (newQuantity <= 0) return;

    setSelectedItems((prevItems) =>
      prevItems.map((item) => {
        if (item.id === itemId) {
          const quantityDiff = newQuantity - item.quantity;
          const availableStock =
            item.product.variantStock?.[item.variantKey] || 0;

          if (quantityDiff > 0 && availableStock < quantityDiff) {
            alert(`Chỉ còn ${availableStock} sản phẩm trong kho!`);
            return item;
          }

          updateStockQuantity(item.product, item.variantKey, -quantityDiff);

          return {
            ...item,
            quantity: newQuantity,
            subtotal: item.price * newQuantity,
          };
        }
        return item;
      }),
    );
  };

  // Xóa sản phẩm khỏi đơn hàng
  const handleRemoveItem = (itemId) => {
    const item = selectedItems.find((i) => i.id === itemId);
    if (item) {
      // Hoàn lại số lượng tồn kho
      updateStockQuantity(item.product, item.variantKey, item.quantity);
    }
    setSelectedItems(selectedItems.filter((i) => i.id !== itemId));
  };

  // Tính toán giá trị đơn hàng
  const subtotal = selectedItems.reduce((sum, item) => sum + item.subtotal, 0);
  const discountAmount = (subtotal * discountPercent) / 100;
  const total = subtotal - discountAmount;
  const change = paymentMethod === "cash" ? Math.max(0, amountPaid - total) : 0;

  // Lưu đơn hàng
  const handleSaveOrder = () => {
    // Validation
    if (!customerInfo.phone) {
      alert("Vui lòng nhập số điện thoại khách hàng!");
      return;
    }

    if (!/^0\d{9}$/.test(customerInfo.phone)) {
      alert("Số điện thoại không hợp lệ! (10 số, bắt đầu bằng 0)");
      return;
    }

    if (selectedItems.length === 0) {
      alert("Vui lòng thêm ít nhất 1 sản phẩm vào đơn hàng!");
      return;
    }

    // Tạo đơn hàng
    const orderNumber = String(Date.now()).slice(-6);
    const newOrder = {
      id: `ORD-${orderNumber}`,
      orderNumber: orderNumber,
      customer: {
        ...customerInfo,
      },
      items: selectedItems.map((item) => ({
        productId: item.productId,
        name: item.name,
        image: item.image,
        color: item.color,
        size: item.size,
        price: item.price,
        quantity: item.quantity,
        subtotal: item.subtotal,
        variantKey: item.variantKey,
      })),
      pricing: {
        subtotal: subtotal,
        discount: discountAmount,
        discountPercent: discountPercent,
        total: total,
      },
      payment: {
        method: paymentMethod,
        status:
          paymentMethod === "cash" && amountPaid >= total ? "paid" : "pending",
        amountPaid: paymentMethod === "cash" ? amountPaid : 0,
        change: change,
      },
      status:
        paymentMethod === "cash" && amountPaid >= total
          ? "completed"
          : "pending",
      createdAt: new Date().toISOString(),
      notes: customerInfo.notesInternal,
    };

    // Lưu vào localStorage
    const savedOrders = localStorage.getItem("ecommerce_orders");
    const orders = savedOrders ? JSON.parse(savedOrders) : [];
    orders.push(newOrder);
    localStorage.setItem("ecommerce_orders", JSON.stringify(orders));

    alert("✅ Đơn hàng đã được lưu thành công!");
    handleReset();
  };

  // Reset form
  const handleReset = () => {
    // Hoàn lại tồn kho cho tất cả sản phẩm trong đơn
    selectedItems.forEach((item) => {
      updateStockQuantity(item.product, item.variantKey, item.quantity);
    });

    setCustomerInfo({
      phone: "",
      name: "",
      address: "",
      city: "",
      district: "",
      ward: "",
      notesForPrint: "",
      notesInternal: "",
    });
    setSelectedItems([]);
    setSearchTerm("");
    setDiscountPercent(0);
    setPaymentMethod("cash");
    setAmountPaid(0);
  };

  return (
    <div className="create-order-container">
      <div className="create-order-content">
        {/* Section Khách hàng */}
        <div className="order-section customer-section">
          <h2 className="section-title">
            <span className="icon">👤</span>
            Khách hàng
          </h2>

          <div className="form-row">
            <div className="form-group">
              <label>
                <span className="icon">📞</span>
                Điện thoại *
              </label>
              <input
                type="tel"
                value={customerInfo.phone}
                onChange={(e) =>
                  setCustomerInfo({ ...customerInfo, phone: e.target.value })
                }
                placeholder="0123456789"
                maxLength="10"
              />
            </div>

            <div className="form-group">
              <label>
                <span className="icon">👤</span>
                Tên khách
              </label>
              <input
                type="text"
                value={customerInfo.name}
                onChange={(e) =>
                  setCustomerInfo({ ...customerInfo, name: e.target.value })
                }
                placeholder="Nhập tên khách hàng"
              />
            </div>
          </div>

          <div className="form-group">
            <label>
              <span className="icon">🏠</span>
              Địa chỉ
            </label>
            <textarea
              value={customerInfo.address}
              onChange={(e) =>
                setCustomerInfo({ ...customerInfo, address: e.target.value })
              }
              placeholder="Nhập địa chỉ"
              rows="2"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>
                <span className="icon">📍</span>
                Thành phố
              </label>
              <select
                value={customerInfo.city}
                onChange={(e) =>
                  setCustomerInfo({ ...customerInfo, city: e.target.value })
                }
              >
                <option value="">- Thành phố -</option>
                {provinces.map((province) => (
                  <option key={province.code} value={province.name}>
                    {province.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>
                <span className="icon">📍</span>
                Quận huyện
              </label>
              <select
                value={customerInfo.district}
                onChange={(e) =>
                  setCustomerInfo({ ...customerInfo, district: e.target.value })
                }
                disabled={!customerInfo.city}
              >
                <option value="">- Quận huyện -</option>
                {districts.map((district) => (
                  <option key={district.code} value={district.name}>
                    {district.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>
              <span className="icon">📍</span>
              Phường xã
            </label>
            <select
              value={customerInfo.ward}
              onChange={(e) =>
                setCustomerInfo({ ...customerInfo, ward: e.target.value })
              }
              disabled={!customerInfo.district}
            >
              <option value="">- Phường xã -</option>
              {wards.map((ward) => (
                <option key={ward.code} value={ward.name}>
                  {ward.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>
              <span className="icon">📝</span>
              Ghi chú khách hàng (Để in)
            </label>
            <textarea
              value={customerInfo.notesForPrint}
              onChange={(e) =>
                setCustomerInfo({
                  ...customerInfo,
                  notesForPrint: e.target.value,
                })
              }
              placeholder="Ghi chú sẽ được in trên hóa đơn"
              rows="2"
            />
          </div>

          <div className="form-group">
            <label>
              <span className="icon">💬</span>
              Ghi chú chăm sóc khách hàng (Nội bộ)
            </label>
            <textarea
              value={customerInfo.notesInternal}
              onChange={(e) =>
                setCustomerInfo({
                  ...customerInfo,
                  notesInternal: e.target.value,
                })
              }
              placeholder="Ghi chú nội bộ, không in trên hóa đơn"
              rows="2"
            />
          </div>
        </div>

        {/* Section Sản phẩm */}
        <div className="order-section products-section">
          <h2 className="section-title">
            <span className="icon">📦</span>
            Sản phẩm
          </h2>

          <div className="product-search-bar">
            <div className="search-input-wrapper">
              <input
                id="product-search"
                type="text"
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setShowProductList(e.target.value.length > 0);
                }}
                placeholder="🔍 (F3) Tìm kiếm sản phẩm"
                onFocus={() => setShowProductList(searchTerm.length > 0)}
              />

              {showProductList && filteredProducts.length > 0 && (
                <div className="product-dropdown">
                  {filteredProducts.slice(0, 10).map((product) => (
                    <div
                      key={product.id}
                      className="product-dropdown-item"
                      onClick={() => handleAddProduct(product)}
                    >
                      <div className="product-dropdown-image">
                        {product.images?.[0] ? (
                          <img src={product.images[0]} alt={product.name} />
                        ) : (
                          <div className="no-image">📦</div>
                        )}
                      </div>
                      <div className="product-dropdown-info">
                        <div className="product-dropdown-name">
                          {product.name}
                        </div>
                        <div className="product-dropdown-price">
                          {(isBanLe
                            ? product.price
                            : product.costPrice || product.price * 0.7
                          ).toLocaleString("vi-VN")}
                          ₫
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="product-actions">
              <button
                className="btn-view-stock"
                onClick={() => setShowStockModal(true)}
              >
                👁️ Xem tồn
              </button>
              <button
                className={`btn-ban-le ${isBanLe ? "active" : ""}`}
                onClick={() => setIsBanLe(!isBanLe)}
              >
                💰 {isBanLe ? "Bán lẻ" : "Bán buôn"}
              </button>
            </div>
          </div>

          {/* Danh sách sản phẩm đã chọn */}
          {selectedItems.length > 0 ? (
            <div className="selected-products">
              <table className="products-table">
                <thead>
                  <tr>
                    <th>Sản phẩm</th>
                    <th>Màu sắc</th>
                    <th>Kích thước</th>
                    <th>Đơn giá</th>
                    <th>Số lượng</th>
                    <th>Thành tiền</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {selectedItems.map((item) => (
                    <tr key={item.id}>
                      <td>
                        <div className="product-info">
                          {item.image ? (
                            <img
                              src={item.image}
                              alt={item.name}
                              className="product-thumb"
                            />
                          ) : (
                            <div className="product-thumb no-image">📦</div>
                          )}
                          <span>{item.name}</span>
                        </div>
                      </td>
                      <td>
                        <select
                          value={item.color}
                          onChange={(e) =>
                            handleColorChange(item.id, e.target.value)
                          }
                          className="variant-select"
                        >
                          {item.product.colors.map((color) => (
                            <option key={color} value={color}>
                              {color}
                            </option>
                          ))}
                        </select>
                        <div className="stock-info">
                          Tồn:{" "}
                          {item.product.variantStock?.[item.variantKey] || 0}
                        </div>
                      </td>
                      <td>
                        <select
                          value={item.size}
                          onChange={(e) =>
                            handleSizeChange(item.id, e.target.value)
                          }
                          className="variant-select"
                        >
                          {item.product.sizes.map((size) => (
                            <option key={size} value={size}>
                              {size}
                            </option>
                          ))}
                        </select>
                      </td>
                      <td className="price-cell">
                        {item.price.toLocaleString("vi-VN")}₫
                      </td>
                      <td>
                        <div className="quantity-input">
                          <button
                            onClick={() =>
                              handleQuantityChange(item.id, item.quantity - 1)
                            }
                            disabled={item.quantity <= 1}
                          >
                            -
                          </button>
                          <input
                            type="number"
                            value={item.quantity}
                            onChange={(e) =>
                              handleQuantityChange(
                                item.id,
                                parseInt(e.target.value) || 1,
                              )
                            }
                            min="1"
                          />
                          <button
                            onClick={() =>
                              handleQuantityChange(item.id, item.quantity + 1)
                            }
                          >
                            +
                          </button>
                        </div>
                      </td>
                      <td className="price-cell subtotal-cell">
                        {item.subtotal.toLocaleString("vi-VN")}₫
                      </td>
                      <td>
                        <button
                          className="btn-remove"
                          onClick={() => handleRemoveItem(item.id)}
                          title="Xóa"
                        >
                          ✕
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-products">
              <div className="empty-icon">🛒</div>
              <p>Chưa có sản phẩm nào được chọn</p>
              <span>Tìm kiếm và thêm sản phẩm vào đơn hàng</span>
            </div>
          )}

          {/* Thanh toán */}
          <div className="payment-section">
            <h3 className="payment-title">💰 Thanh toán</h3>

            <div className="payment-summary">
              <div className="summary-row">
                <span>Tạm tính:</span>
                <span className="summary-value">
                  {subtotal.toLocaleString("vi-VN")}₫
                </span>
              </div>

              <div className="summary-row discount-row">
                <span>Giảm giá:</span>
                <div className="discount-input">
                  <input
                    type="number"
                    value={discountPercent}
                    onChange={(e) =>
                      setDiscountPercent(
                        Math.max(
                          0,
                          Math.min(100, parseFloat(e.target.value) || 0),
                        ),
                      )
                    }
                    min="0"
                    max="100"
                  />
                  <span>%</span>
                  <span className="discount-amount">
                    -{discountAmount.toLocaleString("vi-VN")}₫
                  </span>
                </div>
              </div>

              <div className="summary-row total-row">
                <span>Tổng cộng:</span>
                <span className="summary-value total-value">
                  {total.toLocaleString("vi-VN")}₫
                </span>
              </div>
            </div>

            <div className="payment-method">
              <label>Phương thức thanh toán:</label>
              <div className="payment-options">
                <label
                  className={`payment-option ${paymentMethod === "cash" ? "active" : ""}`}
                >
                  <input
                    type="radio"
                    value="cash"
                    checked={paymentMethod === "cash"}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                  />
                  <span>💵 Tiền mặt</span>
                </label>
                <label
                  className={`payment-option ${paymentMethod === "banking" ? "active" : ""}`}
                >
                  <input
                    type="radio"
                    value="banking"
                    checked={paymentMethod === "banking"}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                  />
                  <span>🏦 Chuyển khoản</span>
                </label>
                <label
                  className={`payment-option ${paymentMethod === "card" ? "active" : ""}`}
                >
                  <input
                    type="radio"
                    value="card"
                    checked={paymentMethod === "card"}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                  />
                  <span>💳 Thẻ</span>
                </label>
              </div>
            </div>

            {paymentMethod === "cash" && (
              <div className="cash-payment">
                <div className="form-group">
                  <label>Khách đưa:</label>
                  <input
                    type="number"
                    value={amountPaid}
                    onChange={(e) =>
                      setAmountPaid(parseFloat(e.target.value) || 0)
                    }
                    placeholder="0"
                  />
                </div>
                <div className="change-amount">
                  <span>Tiền thừa:</span>
                  <span className="change-value">
                    {change.toLocaleString("vi-VN")}₫
                  </span>
                </div>
              </div>
            )}
          </div>

          {/* Action buttons */}
          <div className="order-actions">
            <button className="btn-cancel" onClick={handleReset}>
              ✕ Hủy
            </button>
            <button className="btn-save" onClick={handleSaveOrder}>
              ✓ Lưu đơn hàng
            </button>
          </div>
        </div>
      </div>

      {/* Modal xem tồn kho */}
      {showStockModal && (
        <div className="modal-overlay" onClick={() => setShowStockModal(false)}>
          <div
            className="modal-content stock-modal"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="modal-header">
              <h3>📊 Tồn kho sản phẩm</h3>
              <button
                className="btn-close"
                onClick={() => setShowStockModal(false)}
              >
                ✕
              </button>
            </div>
            <div className="modal-body">
              <div className="stock-list">
                {products.map((product) => {
                  const totalStock = product.variantStock
                    ? Object.values(product.variantStock).reduce(
                        (sum, qty) => sum + qty,
                        0,
                      )
                    : product.stock || 0;

                  return (
                    <div key={product.id} className="stock-item">
                      <div className="stock-item-info">
                        <div className="stock-item-image">
                          {product.images?.[0] ? (
                            <img src={product.images[0]} alt={product.name} />
                          ) : (
                            <div className="no-image">📦</div>
                          )}
                        </div>
                        <div className="stock-item-details">
                          <div className="stock-item-name">{product.name}</div>
                          <div className="stock-item-variants">
                            {product.variantStock &&
                              Object.entries(product.variantStock).map(
                                ([variant, qty]) => {
                                  const [color, size] = variant.split("-");
                                  return (
                                    <span
                                      key={variant}
                                      className="variant-stock"
                                    >
                                      {color}/{size}: <strong>{qty}</strong>
                                    </span>
                                  );
                                },
                              )}
                          </div>
                        </div>
                      </div>
                      <div className="stock-item-total">
                        Tổng: <strong>{totalStock}</strong>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CreateOrder;
