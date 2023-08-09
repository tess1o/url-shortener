import { check } from 'k6';
import http from "k6/http";

export const options = {
    discardResponseBodies: true,
    scenarios: {
        create_urls: {
            executor: 'shared-iterations',
            vus: 30,
            iterations: 10000,
            maxDuration: '30s',
            exec: 'createUrl',

        },
        visit_url: {
            executor: 'shared-iterations',
            vus: 300,
            iterations: 100000,
            maxDuration: '30s',
            exec: 'visitUrl',
        }
    },
};
export function createUrl() {
    const url = 'http://localhost:8080/create';
    const payload = JSON.stringify({
        originalUrl: 'https://amazon.com/123',
        expire: '2000000000',
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);
    check(res, {
        'create status 200': (r) => r.status === 200,
    });
}

export function visitUrl() {
    const url = 'http://localhost:8080/B1Mia51q';

    const res = http.get(url,  { redirects: 0});
    check(res, {
        'visit status 302': (r) => r.status === 302,
    });
}