# fast_pack_fcr
Fast pack by zip's comment


使用方法

1，编写需要的channel（规则单行单个渠道号）,与python脚本同级别目录；
2，为Apk写入注释：python fast_pack_fcr your.apk（注，请先将your.apk的注释清空）；
3，使用java代码读取comment验证写入。

注：该方式适用于v1的jar签名方式，在7.0以后如果使用了v2全包签名，使用comment方式会失效；
注：加固对该方式无影响。
