# Testing Harness for http-based tests.

The purpose of this harness is to be able to execute the same test code for
many server configurations.  Instead of setting up a server fixture in every
test method, the test methods can be executed multiple times with different server
fixtures. 
