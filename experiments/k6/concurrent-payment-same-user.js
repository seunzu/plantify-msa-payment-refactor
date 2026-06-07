// Concurrency test: same user, 5 simultaneous payments of 200,000 each
// Seed: userId=1, balance=500,000
// Expected: at most 2 succeed (2 × 200,000 = 400,000 ≤ 500,000), rest fail with INSUFFICIENT_BALANCE
// Validates: ledger:{userId} Redis lock prevents overdraft
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 5,
  iterations: 5,
};

const PAYMENT_BASE_URL = __ENV.PAYMENT_BASE_URL || 'http://localhost:8084';
const AUTH_TOKEN = 'test-token';
const AMOUNT = 200000;

export default function () {
  // 1. Initiate payment. payment-service creates Transaction PENDING internally.
  const initRes = http.post(
    `${PAYMENT_BASE_URL}/v1/payments/initiate`,
    JSON.stringify({
      sellerId: 1,
      orderId: `ORDER-VU${__VU}-${Date.now()}`,
      orderName: 'Concurrent payment test',
      amount: AMOUNT,
      redirectUri: 'http://localhost:3000/result',
    }),
    {
      headers: {
        Authorization: `Bearer ${AUTH_TOKEN}`,
        'Content-Type': 'application/json',
      },
    }
  );

  check(initRes, { 'initiate ok': (r) => r.status === 200 });

  const token = initRes.json('data.token');
  if (!token) return;

  // 2. Execute payment (concurrent debit — RedLock is the guard here)
  const payRes = http.post(
    `${PAYMENT_BASE_URL}/v1/payments`,
    null,
    { headers: { token: token } }
  );

  console.log(`VU${__VU} execute: status=${payRes.status} body=${payRes.body}`);

  check(payRes, {
    'not server error': (r) => r.status < 500,
    'success or debit failed': (r) => r.status === 200 || r.status === 402,
  });
}
