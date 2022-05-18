Prerequisites to run tests from command line:
- JDK 11+
- maven

<b>Functional tests for todo app rest methods</b>

All tests are in TodoTestIT.kt

<b>How to tun tests from command line:</b>

mvn clean install -Denv=env
Just one environment is configured - "local", so command should be following:

mvn clean install -Denv=local 
<b>or just</b> 

mvn clean install

<b>Performance of `POST /todos` method analysis:</b>

I didn't have a chance to spend a lot of time on this task (so refused to use appropriate tools like
JMeter) and covered two simple scenarios:
- Execute 10,000 sequential requests and analyze response timings
- Execute 20 requests in parallel for 200 times and analyze response timings / errors

Findings:
1. Sequential runs
   1. First post request response time is significantly more than next ones (could be caused by caches warming
   or if database is used then loading of some dbc metadata).
   2. Average response time for 10000 request is 6ms. At the same time there is following statistics:
      Number of requests more than 10ms: 465
      Number of requests more than 20ms: 87
      Number of requests more than 50ms: 18
      Number of requests more than 100ms: 4
   Spikes (response time more than 50ms and 100ms) looks strange and potentially it could be
   an issue, so in real work I would like to understand root cause 
2. Parallel runs
   1. 20 parallel requests are repeated 200 times (4000 requests in total):
      1. Average response time - 21ms (more than 3 times greater in comparison to 
      sequential runs)
      2. 83 requests were not processed (40x error in response)
      3. Response time statistics:
         Number of requests more then 10ms: 3050
         Number of requests more then 20ms: 857
         Number of requests more then 50ms: 124
         Number of requests more then 100ms: 39
      the same as for sequential rins - long processing definitely need to be analyzed.
      
Code of tests in PerformanceIT class.