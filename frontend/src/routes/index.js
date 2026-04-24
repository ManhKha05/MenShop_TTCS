import LayoutAdmin from "../layout/admin"
import LayoutHome from "../layout/user/LayoutHome"
import Dashboard from "../pages/Admin/Dashboard"
import Users from "../pages/Admin/Users"
import AccountLayout from "../pages/User/AccountLayout"
import Cart from "../pages/User/Cart"
import Checkout from "../pages/User/Checkout"
import ForgotPassword from "../pages/User/ForgotPassword"
import Home from "../pages/User/Home"
import Login from "../pages/User/Login"
import MyOrders from "../pages/User/MyOrders"
import OrderDetail from "../pages/User/OrderDetail"
import OrderSuccess from "../pages/User/OrderSuccess"
import OtpPassword from "../pages/User/OtpPassword"
import Product from "../pages/User/Product"
import Profile from "../pages/User/Profile"
import ResetPassword from "../pages/User/ResetPassword"
import Search from "../pages/User/Search"
import Shop from "../pages/User/Shop"
import Signup from "../pages/User/Signup"
import Shops from "../pages/Admin/Shops"
import ShopDetail from "../pages/Admin/ShopDetail"
import Categories from "../pages/Admin/Categories"
import Products from "../pages/Admin/Products"
import ProductDetail from "../components/ProductDetail"
import Orders from "../pages/Admin/Orders"
import LayoutShop from "../layout/shop"
import DashboardShop from "../pages/Shop/Dashboard"
import ProductsShop from "../pages/Shop/Products"
import ProfileShop from "../pages/Shop/Profile"
import OrdersShop from "../pages/Shop/Orders"
import OrderDetailShop from "../pages/Shop/OrderDetail"
import Banners from "../pages/Admin/Banners"
import FlashSale from "../pages/Admin/FlashSale"
import FlashSaleDetail from "../pages/Admin/FlashSaleDetail"
import FlashSaleShop from "../pages/Shop/FlashSale"
import RegisterSell from "../pages/User/RegisterSell"
import Address from "../pages/User/Address"
import EditProduct from "../pages/Shop/EditProduct"
import VnPayReturn from "../pages/User/VnPayReturn"
import PaymentFailed from "../pages/User/PaymentFailed"

import RequireAuth from "../components/RequireAuth";
import RequireRole from "../components/RequireRole";

import Forbidden from "../pages/Error/Forbidden";
import NotFound from "../pages/Error/NotFound";
import ServerError from "../pages/Error/ServerError";
import OAuth2Success from "../pages/User/OAuth2Success"
import ShopReview from "../pages/Shop/Review"
import ShopRevenue from "../pages/Shop/Revenue"
import Category from "../pages/User/Category"

export const routes = [
  {
    path: '/',
    element: <LayoutHome />,
    children: [
      {
        index: true,
        element: <Home />
      },
      {
        path: 'tim-kiem',
        element: <Search />
      },
      {
        path: 'danh-muc/:id',
        element: <Category />
      },
      {
        path: 'san-pham/:id',
        element: <Product />
      },
      {
        path: 'cua-hang/:id',
        element: <Shop />
      },
      {
        path: 'gio-hang',
        element: <Cart />
      },
      {
        path: 'thanh-toan',
        element: (
          <RequireAuth>
            <Checkout />
          </RequireAuth>
        )
      },
      {
        path: 'thanh-toan/vnpay-return',
        element: <VnPayReturn />
      },
      {
        path: 'thanh-toan-that-bai',
        element: <PaymentFailed />
      },
      {
        path: 'dat-hang-thanh-cong',
        element: <OrderSuccess />
      },
      {
        path: 'don-hang/:id',
        element: (
          <RequireAuth>
            <OrderDetail />
          </RequireAuth>
        )
      },
      {
        path: 'tai-khoan',
        element: (
          <RequireAuth>
            <AccountLayout />
          </RequireAuth>
        ),
        children: [
          {
            index: true,
            element: <Profile />
          },
          {
            path: 'don-mua',
            element: <MyOrders />
          },
          {
            path: 'dia-chi',
            element: <Address />
          },
          {
            path: 'dang-ky-ban-hang',
            element: <RegisterSell />
          },
        ]
      },
    ]
  },
  {
    path: '/dang-nhap',
    element: <Login />
  },
  {
    path: '/oauth2/success',
    element: <OAuth2Success />
  },
  {
    path: '/dang-ky',
    element: <Signup />
  },
  {
    path: '/quen-mat-khau',
    element: <ForgotPassword />
  },
  {
    path: '/xac-thuc-otp',
    element: <OtpPassword />
  },
  {
    path: '/dat-lai-mat-khau',
    element: <ResetPassword />
  },
  {
    path: '/admin',
    element: <RequireRole allowRoles={["ROLE_ADMIN"]} />,
    children: [
      {
        path: "/admin",
        element: <LayoutAdmin />,
        children: [
          { index: true, element: <Dashboard /> },
          { path: "users", element: <Users /> },
          { path: "shops", element: <Shops /> },
          { path: "shops/:id", element: <ShopDetail /> },
          { path: "categories", element: <Categories /> },
          { path: "products", element: <Products /> },
          { path: "products/:id", element: <ProductDetail role="ADMIN" /> },
          { path: "orders", element: <Orders /> },
          { path: "flash-sale", element: <FlashSale /> },
          { path: "flash-sale/:id", element: <FlashSaleDetail /> },
          { path: "banners", element: <Banners /> }
        ]
      }
    ]
  },
  {
    path: "shop",
    element: <RequireRole allowRoles={["ROLE_SHOP"]} />,
    children: [
      {
        path: "/shop",
        element: <LayoutShop />,
        children: [
          {
            index: true,
            element: <DashboardShop />
          },
          {
            path: "products",
            element: <ProductsShop />
          },
          {
            path: "products/:id",
            element: <ProductDetail role="SHOP" />
          },
          {
            path: "edit-product",
            element: <EditProduct />
          },
          {
            path: "edit-product/:id",
            element: <EditProduct />
          },
          {
            path: "orders",
            element: <OrdersShop />
          },
          {
            path: "orders/:id",
            element: <OrderDetailShop />
          },
          {
            path: "flash-sale",
            element: <FlashSaleShop />
          },
          {
            path: "revenue",
            element: <ShopRevenue />
          },
          {
            path: "reviews",
            element: <ShopReview />
          },
          {
            path: "profile",
            element: <ProfileShop />
          }
        ]
      }
    ]
  },
  {
    path: "/403",
    element: <Forbidden />
  },
  {
    path: "/500",
    element: <ServerError />
  },
  {
    path: "*",
    element: <NotFound />
  }
]