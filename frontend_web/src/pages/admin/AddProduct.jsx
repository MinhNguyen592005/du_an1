import React, { useState } from "react";
import { useProducts } from "../../context/ProductContext";
import "./AddProduct.css";

const AddProduct = ({ onSuccess, onCancel }) => {
  const { addProduct } = useProducts();

  const [formData, setFormData] = useState({
    name: "",
    price: "",
    costPrice: "",
    colors: [],
    sizes: [],
    description: "",
    category: "",
    variantStock: {},
  });

  const [images, setImages] = useState([]);
  const [previewImages, setPreviewImages] = useState([]);
  const [colorInput, setColorInput] = useState("");
  const [sizeInput, setSizeInput] = useState("");

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleVariantStockChange = (color, size, quantity) => {
    const variantKey = `${color}-${size}`;
    setFormData({
      ...formData,
      variantStock: {
        ...formData.variantStock,
        [variantKey]: parseInt(quantity) || 0,
      },
    });
  };

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);

    files.forEach((file) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewImages((prev) => [...prev, reader.result]);
        setImages((prev) => [...prev, reader.result]);
      };
      reader.readAsDataURL(file);
    });
  };

  const removeImage = (index) => {
    setImages(images.filter((_, i) => i !== index));
    setPreviewImages(previewImages.filter((_, i) => i !== index));
  };

  const addColor = () => {
    if (colorInput.trim() && !formData.colors.includes(colorInput.trim())) {
      const newColor = colorInput.trim();
      const newVariantStock = { ...formData.variantStock };

      formData.sizes.forEach((size) => {
        const variantKey = `${newColor}-${size}`;
        if (!newVariantStock[variantKey]) {
          newVariantStock[variantKey] = 0;
        }
      });

      setFormData({
        ...formData,
        colors: [...formData.colors, newColor],
        variantStock: newVariantStock,
      });
      setColorInput("");
    }
  };

  const removeColor = (colorToRemove) => {
    const newVariantStock = { ...formData.variantStock };

    Object.keys(newVariantStock).forEach((key) => {
      if (key.startsWith(`${colorToRemove}-`)) {
        delete newVariantStock[key];
      }
    });

    setFormData({
      ...formData,
      colors: formData.colors.filter((color) => color !== colorToRemove),
      variantStock: newVariantStock,
    });
  };

  const addSize = () => {
    if (sizeInput.trim() && !formData.sizes.includes(sizeInput.trim())) {
      const newSize = sizeInput.trim();
      const newVariantStock = { ...formData.variantStock };

      formData.colors.forEach((color) => {
        const variantKey = `${color}-${newSize}`;
        if (!newVariantStock[variantKey]) {
          newVariantStock[variantKey] = 0;
        }
      });

      setFormData({
        ...formData,
        sizes: [...formData.sizes, newSize],
        variantStock: newVariantStock,
      });
      setSizeInput("");
    }
  };

  const removeSize = (sizeToRemove) => {
    const newVariantStock = { ...formData.variantStock };

    Object.keys(newVariantStock).forEach((key) => {
      if (key.endsWith(`-${sizeToRemove}`)) {
        delete newVariantStock[key];
      }
    });

    setFormData({
      ...formData,
      sizes: formData.sizes.filter((size) => size !== sizeToRemove),
      variantStock: newVariantStock,
    });
  };

  const getTotalStock = () => {
    if (Object.keys(formData.variantStock).length > 0) {
      return Object.values(formData.variantStock).reduce(
        (sum, qty) => sum + (parseInt(qty) || 0),
        0,
      );
    }
    return 0;
  };

  const getTotalByColor = (color) => {
    let total = 0;
    Object.entries(formData.variantStock).forEach(([key, qty]) => {
      if (key.startsWith(`${color}-`)) {
        total += parseInt(qty) || 0;
      }
    });
    return total;
  };

  const getTotalBySize = (size) => {
    let total = 0;
    Object.entries(formData.variantStock).forEach(([key, qty]) => {
      if (key.endsWith(`-${size}`)) {
        total += parseInt(qty) || 0;
      }
    });
    return total;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.name || !formData.price) {
      alert("Vui lòng điền đầy đủ thông tin bắt buộc!");
      return;
    }

    try {
      const newProduct = {
        ...formData,
        images: images,
        price: parseFloat(formData.price),
        costPrice: parseFloat(formData.costPrice),
        stock: getTotalStock(),
      };

      console.log("AddProduct: Submitting product:", newProduct);
      addProduct(newProduct);

      alert("Thêm sản phẩm thành công!");

      setFormData({
        name: "",
        price: "",
        costPrice: "",
        colors: [],
        sizes: [],
        description: "",
        category: "",
        variantStock: {},
      });
      setImages([]);
      setPreviewImages([]);

      if (onSuccess) {
        onSuccess();
      }
    } catch (error) {
      console.error("Error adding product:", error);
      alert("Có lỗi xảy ra khi thêm sản phẩm!");
    }
  };

  return (
    <div className="add-product-container">
      <h1>Thêm Sản Phẩm Mới</h1>

      <form onSubmit={handleSubmit} className="add-product-form">
        <div className="form-group">
          <label>Hình Ảnh Sản Phẩm</label>
          <div className="image-upload-area">
            <input
              type="file"
              id="imageUpload"
              multiple
              accept="image/*"
              onChange={handleImageChange}
              style={{ display: "none" }}
            />
            <label htmlFor="imageUpload" className="upload-label">
              <div className="upload-icon">📷</div>
              <p>Chọn hoặc kéo thả ảnh vào đây</p>
              <span>Hỗ trợ: JPG, PNG, GIF</span>
            </label>
          </div>

          {previewImages.length > 0 && (
            <div className="image-preview-container">
              {previewImages.map((preview, index) => (
                <div key={index} className="image-preview">
                  <img src={preview} alt={`Preview ${index + 1}`} />
                  <button
                    type="button"
                    className="remove-image-btn"
                    onClick={() => removeImage(index)}
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="name">Tên Sản Phẩm *</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleInputChange}
            placeholder="Nhập tên sản phẩm"
            required
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="costPrice">Giá Vốn (VNĐ) *</label>
            <input
              type="number"
              id="costPrice"
              name="costPrice"
              value={formData.costPrice}
              onChange={handleInputChange}
              placeholder="Nhập giá vốn"
              min="0"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="price">Giá Bán (VNĐ) *</label>
            <input
              type="number"
              id="price"
              name="price"
              value={formData.price}
              onChange={handleInputChange}
              placeholder="Nhập giá bán"
              min="0"
              required
            />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="category">Danh Mục</label>
          <select
            id="category"
            name="category"
            value={formData.category}
            onChange={handleInputChange}
          >
            <option value="">Chọn danh mục</option>
            <option value="ao">Áo</option>
            <option value="quan">Quần</option>
            <option value="giay">Giày</option>
            <option value="phukien">Phụ kiện</option>
          </select>
        </div>

        <div className="form-group">
          <label>Màu Sắc</label>
          <div className="input-with-button">
            <input
              type="text"
              value={colorInput}
              onChange={(e) => setColorInput(e.target.value)}
              placeholder="Nhập màu sắc"
              onKeyPress={(e) =>
                e.key === "Enter" && (e.preventDefault(), addColor())
              }
            />
            <button type="button" onClick={addColor} className="add-btn">
              Thêm
            </button>
          </div>
          <div className="tags-container">
            {formData.colors.map((color, index) => (
              <span key={index} className="tag">
                {color}
                <button type="button" onClick={() => removeColor(color)}>
                  ✕
                </button>
              </span>
            ))}
          </div>
        </div>

        <div className="form-group">
          <label>Kích Thước</label>
          <div className="input-with-button">
            <input
              type="text"
              value={sizeInput}
              onChange={(e) => setSizeInput(e.target.value)}
              placeholder="Nhập kích thước (S, M, L, XL...)"
              onKeyPress={(e) =>
                e.key === "Enter" && (e.preventDefault(), addSize())
              }
            />
            <button type="button" onClick={addSize} className="add-btn">
              Thêm
            </button>
          </div>
          <div className="tags-container">
            {formData.sizes.map((size, index) => (
              <span key={index} className="tag">
                {size}
                <button type="button" onClick={() => removeSize(size)}>
                  ✕
                </button>
              </span>
            ))}
          </div>
        </div>

        {formData.colors.length > 0 && formData.sizes.length > 0 && (
          <div className="variant-stock-section">
            <h4>📊 Số Lượng Theo Màu Sắc và Kích Thước</h4>
            <div className="variant-table-container">
              <table className="variant-table">
                <thead>
                  <tr>
                    <th>Màu / Size</th>
                    {formData.sizes.map((size) => (
                      <th key={size}>{size}</th>
                    ))}
                    <th className="total-column">Tổng</th>
                  </tr>
                </thead>
                <tbody>
                  {formData.colors.map((color) => (
                    <tr key={color}>
                      <td className="color-label">
                        <strong>{color}</strong>
                      </td>
                      {formData.sizes.map((size) => {
                        const variantKey = `${color}-${size}`;
                        return (
                          <td key={size}>
                            <input
                              type="number"
                              value={formData.variantStock[variantKey] || 0}
                              onChange={(e) =>
                                handleVariantStockChange(
                                  color,
                                  size,
                                  e.target.value,
                                )
                              }
                              min="0"
                              className="variant-input"
                            />
                          </td>
                        );
                      })}
                      <td className="total-cell">{getTotalByColor(color)}</td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr>
                    <td>
                      <strong>Tổng</strong>
                    </td>
                    {formData.sizes.map((size) => (
                      <td key={size} className="total-cell">
                        {getTotalBySize(size)}
                      </td>
                    ))}
                    <td className="grand-total">{getTotalStock()}</td>
                  </tr>
                </tfoot>
              </table>
            </div>
            <div className="total-stock-info">
              <strong>🔢 Tổng số lượng tất cả: {getTotalStock()}</strong>
            </div>
          </div>
        )}

        {(formData.colors.length === 0 || formData.sizes.length === 0) && (
          <div className="variant-note">
            <p>
              💡 Vui lòng thêm cả <strong>Màu sắc</strong> và{" "}
              <strong>Kích thước</strong> để nhập số lượng chi tiết
            </p>
          </div>
        )}

        <div className="form-group">
          <label htmlFor="description">Mô Tả Sản Phẩm</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleInputChange}
            placeholder="Nhập mô tả chi tiết về sản phẩm"
            rows="5"
          />
        </div>

        <div className="form-actions">
          <button type="button" onClick={onCancel} className="cancel-btn">
            Hủy
          </button>
          <button type="submit" className="submit-btn">
            ➕ Thêm Sản Phẩm
          </button>
        </div>
      </form>
    </div>
  );
};

export default AddProduct;
