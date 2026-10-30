package org.cmarket.cmarket.web.eventfeed;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IngestKeyVerifierTest {

    private final IngestKeyVerifier verifier = new IngestKeyVerifier("secret-key");

    @Test
    void 같은_키면_통과() {
        assertThat(verifier.verify("secret-key")).isTrue();
    }

    @Test
    void 다른_키면_거절() {
        assertThat(verifier.verify("secret-kez")).isFalse();
        assertThat(verifier.verify("secret")).isFalse();
    }

    @Test
    void 키가_없으면_거절() {
        assertThat(verifier.verify(null)).isFalse();
        assertThat(verifier.verify("")).isFalse();
    }

    @Test
    void 서버에_키가_없으면_무엇이든_거절() {
        IngestKeyVerifier empty = new IngestKeyVerifier("");
        assertThat(empty.isConfigured()).isFalse();
        assertThat(empty.verify("")).isFalse();
        assertThat(empty.verify("anything")).isFalse();
        assertThat(verifier.isConfigured()).isTrue();
    }
}
