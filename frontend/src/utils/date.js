import dayjs from "dayjs";

export const formatDateTime = (date) => (
  dayjs(date).format('HH:mm DD/MM/YYYY')
)

export const formatDateTime2 = (dateString) => {
  if (!dateString) return "";
  return new Date(dateString).toLocaleString("vi-VN");
};

export const formatDate = (date) => (
  dayjs(date).format('DD/MM/YYYY')
)