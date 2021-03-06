package no.finn.unleash;

import java.net.URI;
import java.util.Random;

import no.finn.unleash.repository.*;

public class ManualTesting {
    public static void main(String[] args) throws Exception {
        ToggleFetcher toggleFetcher = new HttpToggleFetcher(URI.create("http://localhost:4242/features"));
        ToggleBackupHandler toggleBackupHandler = new ToggleBackupHandlerFile();
        ToggleRepository repository = new FeatureToggleRepository(toggleFetcher, toggleBackupHandler, 1);
        Unleash unleash = new DefaultUnleash(repository);

        for(int i=0;i<100;i++) {
            (new Thread(new UnleashThread(unleash, "thread-"+i, 100))).start();
        }
    }

    static class UnleashThread implements Runnable {

        final Unleash unleash;
        final String name;
        final int maxRounds;
        int currentRound = 0;

        UnleashThread(Unleash unleash, String name, int maxRounds) {
            this.unleash = unleash;
            this.name = name;
            this.maxRounds = maxRounds;
        }

        public void run() {
            while(currentRound < maxRounds) {
                currentRound++;
                long startTime = System.nanoTime();
                boolean enabled = unleash.isEnabled("featureX");
                long timeUsed = System.nanoTime() - startTime;

                System.out.println(name + "\t" +"featureX" +":"  + enabled + "\t " + timeUsed + "ns");

                try {
                    //Wait 1 to 10ms before next round
                    Thread.sleep(new Random().nextInt(10000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
