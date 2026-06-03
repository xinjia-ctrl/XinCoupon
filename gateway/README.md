# XinCoupon Gateway

独立网关工程，用于将优惠券主应用的核心接口统一暴露到 `8088` 端口。

## 启动

先启动主应用，再启动网关：

```powershell
mvn -f gateway/pom.xml spring-boot:run
```

默认转发目标为 `http://localhost:8080`，可以通过参数覆盖：

```powershell
mvn -f gateway/pom.xml spring-boot:run `
  "-Dspring-boot.run.jvmArguments=-DXIN_COUPON_SERVICE_URI=http://localhost:8080 -DGATEWAY_PORT=8088"
```

如需接入 Nacos：

```powershell
mvn -f gateway/pom.xml spring-boot:run `
  "-Dspring-boot.run.jvmArguments=-DNACOS_DISCOVERY_ENABLED=true -DNACOS_SERVER_ADDR=<Nacos地址>"
```
