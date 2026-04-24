import './App.css';
import AllRoutes from '../src/components/AllRoutes';
import ScrollToTop from "./components/ScrollToTop"
import { CartProvider } from './components/CartContext';

function App() {
  return (
    <>
      <CartProvider>
        <ScrollToTop />
        <AllRoutes />
      </CartProvider>

    </>
  );
}

export default App;
