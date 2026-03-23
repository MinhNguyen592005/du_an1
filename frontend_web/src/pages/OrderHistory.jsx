import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { orderAPI } from "../services/api";
import "./OrderHistory.css";

const OrderHistory = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState("");
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const pageSize = 10;

  useEffect(() => {
    document.title = "Lịch sử đơn hàng - E-Commerce";
    loadOrders();
  }, [currentPage]);

  const loadOrders = async () => {
    try {
      setLoading(true);
      setError("");
      const userId = localStorage.getItem("userId");

      const response = await orderAPI.getByUserId(
        userId,
        currentPage,
        pageSize,
      );

      if (response.code === 1000) {
        setOrders(response.result.content || []);
        setTotalPages(response.result.totalPages || 0);
      } else {
        setError("Không thể tải danh sách đơn hàng");
      }
    } catch (err) {
      console.error("Error loading orders:", err);
      setError("Vui lòng đăng nhập để xem lịch sử đơn hàng");
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(value || 0);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString("vi-VN");
  };

  const getStatusText = (status) => {
    const statusMap = {
      PENDING: "Chờ xác nhận",
      CONFIRMED: "Đã xác nhận",
      SHIPPING: "Đang giao",
      DELIVERED: "Đã giao",
      CANCELLED: "Đã hủy",
    };
    return statusMap[status] || status;
  };

  const getStatusColor = (status) => {
    const colorMap = {
      PENDING: "warning",
      CONFIRMED: "info",
      SHIPPING: "primary",
      DELIVERED: "success",
      CANCELLED: "danger",
    };
    return colorMap[status] || "default";
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setCurrentPage(newPage);
      window.scrollTo({ top: 0, behavior: "smooth" });
    }
  };

  const handleViewDetail = async (order) => {
    try {
      // Load full order details to get address info
      const response = await orderAPI.getById(order.id);
      if (response.code === 1000) {
        setSelectedOrder(response.result);
        setShowDetailModal(true);
      } else {
        setError("Không thể tải chi tiết đơn hàng");
      }
    } catch (err) {
      console.error("Error loading order detail:", err);
      setError("Đã xảy ra lỗi khi tải chi tiết đơn hàng");
    }
  };

  const closeModal = () => {
    setShowDetailModal(false);
    setSelectedOrder(null);
  };

  if (loading) {
    return (
      <div className="order-history-page">
        <div className="loading">
          <div className="spinner"></div>
          <p>Đang tải lịch sử đơn hàng...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="order-history-page">
        <div className="error-message">
          <h2>⚠️ {error}</h2>
          <button onClick={() => navigate("/login")}>Đăng nhập</button>
        </div>
      </div>
    );
  }

  return (
    <div className="order-history-page">
      <div className="page-header">
        <h1>Lịch sử đơn hàng</h1>
        <p>Quản lý và theo dõi các đơn hàng của bạn</p>
      </div>

      {orders.length === 0 ? (
        <div className="empty-orders">
          <div className="empty-icon">📦</div>
          <h2>Chưa có đơn hàng nào</h2>
          <p>Hãy bắt đầu mua sắm ngay!</p>
          <button onClick={() => navigate("/")}>Khám phá sản phẩm</button>
        </div>
      ) : (
        <>
          <div className="orders-list">
            {orders.map((order) => (
              <div key={order.id} className="order-item-row">
                <div className="order-info">
                  <div className="order-number-inline">
                    <strong>#{order.orderNumber}</strong>
                  </div>
                  <div className="order-date-inline">
                    {formatDate(order.placedAt)}
                  </div>
                  <span
                    className={`status-badge ${getStatusColor(order.status)}`}
                  >
                    {getStatusText(order.status)}
                  </span>
                  <div className="order-total-inline">
                    {formatCurrency(order.total)}
                  </div>
                  <div className="order-items-count">
                    {order.items?.length || 0} sản phẩm
                  </div>
                </div>

                <div className="order-actions">
                  <button
                    className="btn-view-detail"
                    onClick={() => handleViewDetail(order)}
                  >
                    Xem chi tiết
                  </button>

                  {order.status === "PENDING" && (
                    <button className="btn-cancel">Hủy</button>
                  )}

                  {order.status === "DELIVERED" && (
                    <button
                      className="btn-reorder"
                      onClick={() => navigate("/")}
                    >
                      Mua lại
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="pagination">
              <button
                className="btn-page"
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
              >
                ← Trước
              </button>

              <div className="page-numbers">
                {[...Array(totalPages)].map((_, index) => (
                  <button
                    key={index}
                    className={`btn-page ${currentPage === index ? "active" : ""}`}
                    onClick={() => handlePageChange(index)}
                  >
                    {index + 1}
                  </button>
                ))}
              </div>

              <button
                className="btn-page"
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages - 1}
              >
                Sau →
              </button>
            </div>
          )}
        </>
      )}

      {/* Detail Modal */}
      {showDetailModal && selectedOrder && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Chi tiết đơn hàng #{selectedOrder.orderNumber}</h2>
              <button className="modal-close" onClick={closeModal}>
                ✕
              </button>
            </div>

            <div className="modal-body">
              <div className="detail-section">
                <h3>Thông tin đơn hàng</h3>
                <div className="detail-row">
                  <span>Ngày đặt:</span>
                  <strong>{formatDate(selectedOrder.placedAt)}</strong>
                </div>
                <div className="detail-row">
                  <span>Trạng thái:</span>
                  <span
                    className={`status-badge ${getStatusColor(selectedOrder.status)}`}
                  >
                    {getStatusText(selectedOrder.status)}
                  </span>
                </div>
              </div>

              <div className="detail-section">
                <h3>Địa chỉ giao hàng</h3>
                <div className="address-info">
                  <p>
                    <strong>Nhãn:</strong>{" "}
                    {selectedOrder.address?.label || "N/A"}
                  </p>
                  <p>
                    <strong>Địa chỉ:</strong>{" "}
                    {selectedOrder.address?.street || "N/A"}
                  </p>
                  <p>
                    <strong>Phường/Xã:</strong>{" "}
                    {selectedOrder.address?.postalCode || "N/A"}
                  </p>
                  <p>
                    <strong>Quận/Huyện:</strong>{" "}
                    {selectedOrder.address?.state || "N/A"}
                  </p>
                  <p>
                    <strong>Tỉnh/Thành:</strong>{" "}
                    {selectedOrder.address?.city || "N/A"}
                  </p>
                  <p>
                    <strong>Quốc gia:</strong>{" "}
                    {selectedOrder.address?.country || "N/A"}
                  </p>
                </div>
              </div>

              <div className="detail-section">
                <h3>Sản phẩm</h3>
                <div className="modal-items-list">
                  {selectedOrder.items?.map((item, index) => (
                    <div key={index} className="modal-item">
                      <div className="modal-item-info">
                        <span className="modal-item-name">
                          {item.productName}
                        </span>
                        <span className="modal-item-price">
                          {formatCurrency(item.unitPrice)} x {item.quantity}
                        </span>
                      </div>
                      <strong className="modal-item-total">
                        {formatCurrency(item.unitPrice * item.quantity)}
                      </strong>
                    </div>
                  ))}
                </div>
              </div>

              <div className="detail-section">
                <div className="detail-row total-row">
                  <span>Tổng cộng:</span>
                  <strong className="total-amount">
                    {formatCurrency(selectedOrder.total)}
                  </strong>
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button className="btn-close-modal" onClick={closeModal}>
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrderHistory;
