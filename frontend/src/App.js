import './App.css';
import AllRoutes from '../src/components/AllRoutes';
import ScrollToTop from "./components/ScrollToTop"
import { CartProvider } from './components/CartContext';
import { ChatProvider } from './components/ChatContext';

function App() {
  return (
    <>
      <ChatProvider >
        <CartProvider>
          <ScrollToTop />
          <AllRoutes />
        </CartProvider>
      </ChatProvider>
    </>
  );
}

export default App;
