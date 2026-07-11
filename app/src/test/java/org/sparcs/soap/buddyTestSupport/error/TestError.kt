package org.sparcs.soap.buddyTestSupport.error

sealed class TestError : Exception() {
    class TestFailure : TestError() {
        override val message: String = "Test failure"
    }

    class NotConfigured : TestError() {
        override val message: String = "Not configured"
    }
}