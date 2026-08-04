import http from 'k6/http';
import { check, sleep } from 'k6';


export const options = {

    // ADV158:
    // 200 concurrent users for 60 seconds

    scenarios: {

        trade_creation_load: {

            executor: 'constant-vus',

            vus: 200,

            duration: '60s'

        }

    },


    thresholds: {

        http_req_failed: [
            'rate<0.01'
        ],

        http_req_duration: [
            'p(95)<1000'
        ]

    }

};



const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';



export default function () {


    const payload = JSON.stringify({

        tradeRef: `K6-${__VU}-${__ITER}`,

        instrumentSymbol: "AAPL",

        quantity: 100,

        price: 150.50,

        tradeDate: "2026-08-04",

        counterpartyLei: "5493001KJTIIGC8Y1R12"

    });



    const response = http.post(

        `${BASE_URL}/api/v1/trades`,

        payload,

        {

            headers: {

                'Content-Type':
                'application/json'

            }

        }

    );



    check(response, {

        'trade creation successful':
        (r) => r.status === 200 || r.status === 201

    });


    sleep(1);

}