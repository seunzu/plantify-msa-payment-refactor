import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 5,
  iterations: 5,
};

const BASE_URL = __ENV.PAYMENT_BASE_URL || 'http://localhost:8084';
const AUTH_TOKEN = 'test-token';
const ORDER_ID = 'ORDER-DUPLICATE-99999';

export default function () {
  const response = http.post(
    `${BASE_URL}/v1/payments/initiate`,
    JSON.stringify({
      sellerId: 1,
      orderId: ORDER_ID,
      orderName: 'Duplicate transaction test',
      amount: 10000,
      redirectUri: 'http://localhost:3000/result',
    }),
    {
      headers: {
        Authorization: `Bearer ${AUTH_TOKEN}`,
        'Content-Type': 'application/json',
      },
      tags: { scenario: 'duplicate-transaction' },
    }
  );

  console.log(`status=${response.status} body=${response.body}`);

  check(response, {
    'success or conflict': (r) => r.status === 200 || r.status === 409,
  });
}
