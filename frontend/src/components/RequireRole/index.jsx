import { Navigate, Outlet } from "react-router-dom";

function RequireRole({ allowRoles = [] }) {
  const token = localStorage.getItem("token");
  const roles = JSON.parse(localStorage.getItem("roles") || "[]");

  if (!token) {
    return <Navigate to="/dang-nhap" replace />;
  }

  const hasRole = allowRoles.some(role => roles.includes(role));

  if (!hasRole) {
    return <Navigate to="/403" replace />;
  }

  return <Outlet />;
}

export default RequireRole;