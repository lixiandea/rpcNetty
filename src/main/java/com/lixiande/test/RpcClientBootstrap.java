package com.lixiande.test;

import com.lixiande.client.RpcClient;
import com.lixiande.common.service.discovery.ServiceDiscovery;
import com.lixiande.test.service.HelloService;

public class RpcClientBootstrap {



    public static void main(String[] args) throws InterruptedException {
        final RpcClient client = new RpcClient(new ServiceDiscovery("127.0.0.1:2181"));
        int threadNum = 5;
        final int requestNum = 50;
        Thread[] threads = new Thread[threadNum];

        long startTime = System.currentTimeMillis();
        //benchmark for sync call
        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < requestNum; i++) {
                        try {
                            final HelloService syncClient = client.createService(HelloService.class, "1.0");
                            String result = syncClient.hello(Integer.toString(i));
                            if (!result.equals("Hello " + i)) {
                                System.out.println("error = " + result);
                            } else {
                                System.out.println("result = " + result);
                            }
                            try {
                                Thread.sleep(5 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception ex) {
                            System.out.println(ex.toString());
                        }
                    }
                }
            });
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        long timeCost = (System.currentTimeMillis() - startTime);
        String msg = String.format("Sync call total-time-cost:%sms, req/s=%s", timeCost, ((double) (requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);

        client.stop();
    }

}
