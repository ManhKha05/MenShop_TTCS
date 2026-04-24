import UserFooter from "./UserFooter";
import UserHeader from "./UserHeader";
import { Outlet } from "react-router-dom"

function LayoutHome() {
  return (
    <>
      <UserHeader />
      <div style={{marginTop: '95px', backgroundColor: '#f6f6f8'}}>
        <Outlet />
      </div>
      <UserFooter/>
    </>
  )
}

export default LayoutHome;