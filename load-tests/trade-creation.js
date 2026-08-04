import http from 'k6/http';
import { check, sleep } from 'k6';


export const options = {

    stages: [
        {
            duration: '10s',
            target: 200
        },
        {
            duration: '60s',
            target: 200
        },
        {
            duration: '10s',
            target: 0
        }
    ],

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



export function setup() {

    const loginResponse = http.post(

        `${BASE_URL}/api/auth/login`,

        JSON.stringify({

            email: "trader@db.com",

            password: "trader123"

        }),

        {

            headers: {

                'Content-Type':
                'application/json'

            }

        }

    );


    return {

        token:
        loginResponse.json('accessToken')

    };

}



export default function (data) {


    const tradePayload = JSON.stringify({

        tradeRef:
        `K6-${__VU}-${__ITER}`,

        instrumentSymbol:
        "SAP.DE",

        counterpartyLei:
        "5493001ABCDE12345001",

        quantity:
        100,

        price:
        250.50,

        tradeDate:
        "2026-06-02"

    });



    const response = http.post(

        `${BASE_URL}/api/v1/trades`,

        tradePayload,

        {

            headers: {

                'Content-Type':
                'application/json',

                'Authorization':
                `Bearer ${data.token}`

            }

        }

    );



    check(response, {

        "trade created":
        (r) => r.status === 200 || r.status === 201

    });


    sleep(1);

}