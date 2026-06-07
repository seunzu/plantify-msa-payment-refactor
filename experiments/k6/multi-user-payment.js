import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 5,
  iterations: 5,
};

const BASE_URL = __ENV.PAYMENT_BASE_URL || 'http://localhost:8084';
const TOKENS = (__ENV.PAYMENT_TOKENS || 'test-token-1,test-token-2,test-token-3,test-token-4,test-token-5')
  .split(',')
  .filter(Boolean);
const AMOUNT = 10000;

export default function () {
  const authToken = TOKENS[(__VU - 1) % TOKENS.length];

  const initRes = http.post(
    `${BASE_URL}/v1/payments/initiate`,
    JSON.stringify({
      sellerId: 1,
      orderId: `ORDER-MULTI-VU${__VU}-${Date.now()}`,
      orderName: 'Multi user payment test',
      amount: AMOUNT,
      redirectUri: 'http://localhost:3000/result',
    }),
    {
      headers: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'application/json',
      },
      tags: {
        scenario: 'multi-user-initiate',
      },
    }
  );

  check(initRes, {
    'initiate ok': (res) => res.status === 200,
  });

  const paymentToken = initRes.json('data.token');
  if (!paymentToken) return;

  const response = http.post(
    `${BASE_URL}/v1/payments`,
    null,
    {
      headers: {
        token: paymentToken,
      },
      tags: {
        scenario: 'multi-user-payment',
      },
    }
  );

  check(response, {
    'payment approved': (res) => res.status === 200,
  });
}
