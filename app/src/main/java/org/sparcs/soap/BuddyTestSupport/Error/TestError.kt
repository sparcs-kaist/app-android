package org.sparcs.soap.BuddyTestSupport.Error

sealed class TestError : Exception() {
    data object TestFailure : TestError() {
        private fun readResolve(): Any = TestFailure
        override val message: String = "Test failure"
    }

    data object NotConfigured : TestError() {
        private fun readResolve(): Any = NotConfigured
        override val message: String = "Not configured"
    }
}