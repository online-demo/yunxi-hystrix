hystrix支持N个请求自动合并为一个请求，这个功能在有网络交互的场景下尤其有用，
比如每个请求都要网络访问远程资源，如果把请求合并为一个，将使多次网络交互变成一次，
极大节省开销。重要一点，两个请求能自动合并的前提是两者足够“近”，
即两者启动执行的间隔时长要足够小，默认为10ms，即超过10ms将不自动合并。

以demo为例，我们连续发起多个queue请求，依次返回f1~f6共6个Future对象，
根据打印结果可知f1~f5同处一个线程，说明这5个请求被合并了，而f6由另一个线程执行，
这是因为f5和f6中间隔了一个sleep，超过了合并要求的最大间隔时长。
