package au.com.dius.pact.consumer;

import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.PactFragment;
import org.junit.Test;

import java.io.IOException;

public abstract class ConsumerPactTest {
    public static VerificationResult PACT_VERIFIED = PactVerified$.MODULE$;

    protected abstract PactFragment createFragment(ConsumerPactBuilder.PactDslWithProvider builder);
    protected abstract String providerName();
    protected abstract String consumerName();

    protected abstract void runTest(String url) throws IOException;

    @Test
    public void testPact() {
        PactFragment fragment = createFragment(ConsumerPactBuilder.consumer(consumerName()).hasPactWith(providerName()));
        final MockProviderConfig config = MockProviderConfig.createDefault();

        VerificationResult result = fragment.runConsumer(config, new TestRun() {
            public void run(MockProviderConfig config) throws IOException {
                runTest(config.url());
            }
        });

        if (!result.equals(PACT_VERIFIED)) {
            if (result instanceof PactError) {
                throw new RuntimeException(((PactError)result).error());
            }
            if (result instanceof UserCodeFailed) {
                throw new RuntimeException(((UserCodeFailed<RuntimeException>)result).error());
            }
            if (result instanceof PactMismatch) {
                throw new PactMismatchException(((PactMismatch)result));
            }
        }
    }
}
