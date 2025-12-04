import React from 'react';
import { createRoot } from 'react-dom/client';
import ApplePayButton from './ApplePayButton';

const App = () => (
  <div style={{padding:40}}>
    <h2>Apple Pay + Stripe Demo (Custom Button)</h2>
    <ApplePayButton amountCents={1990} currency={"usd"} label={"Demo Product"} />
  </div>
);

const container = document.getElementById('root');
const root = createRoot(container);
root.render(<App />);
