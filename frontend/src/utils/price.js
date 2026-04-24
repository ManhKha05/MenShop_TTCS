export const formatPrice = (price) => {
  if (!price && price !== 0) return "";
  
  return new Intl.NumberFormat("vi-VN").format(price) + "₫";
};

export const formatPrice2 = (price) => {
  if (!price && price !== 0) return "";
  
  return new Intl.NumberFormat("vi-VN").format(price) + "đ";
};