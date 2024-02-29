# 인프런 강의

해당 저장소의 `README.md`는 인프런 김영한님의 SpringBoot 강의 시리즈를 듣고 Spring 프레임워크의 방대한 기술들을 복기하고자 공부한 내용을 가볍게 정리한 것입니다.

문제가 될 시 삭제하겠습니다.



## 해당 프로젝트에서 배우는 내용

- 섹션 10 | 스프링 타입 컨버터



# 섹션 10 | 스프링 타입 컨버터

## 스프링 타입 컨버터 소개

애플리케이션을 개발하다 보면 타입을 변환해야 하는 경우가 상당히 많다.
<u>스프링에서 제공하는 타입 컨버터는 기본적으로 수많은 타입 변환을 지원하며, 추가적으로 사용자 정의의 타입을 변경하고 싶을 때 사용된다.</u> 



### 예제 - @RequesetParam

* 해당 어노테이션은 스프링이 중간에서 타입을 변환해 준 것이다.
* `@ModelAttribute`, `@PathVariable`도 마찬가지로 타입 변환을 지원한다.

```java
 @GetMapping("/hello-v2")
 public String helloV2(@RequestParam Integer data) {
     System.out.println("data = " + data);
     return "ok";
 }
```



#### 스프링의 타입 변환 적용 예

* 스프링 MVC 요청 파라미터(이것 이외에 아래 방식은 어떤 것인지 잘 모르겠다. 나중에 공부하기로..)
  * `@RequestParam` , `@ModelAttribute` , `@PathVariable`
*  `@Value` 등으로 YML 정보 읽기
*  XML에 넣은 스프링 빈 정보를 변환
* 뷰를 렌더링 할 때



### 컨버터 인터페이스

스프링은 확장 가능한 컨버터 인터페이스를 제공한다.
**개발자는 스프링에 추가적인 타입 변환이 필요하면 이 컨버터 인터페이스를 구현해서 등록하면 된다.**

```java
package org.springframework.core.convert.converter;
    public interface Converter<S, T> {
    	T convert(S source);
 }
```



## 타입 컨버터 - Converter



### 예제 - 사용자 정의 컨버터

IP, PORT를 입력하면 IpPort 객체로 변환하는 컨버터로 만들기

[`IpPort`]

* 객체 정의

```java
package hello.typeconverter.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class IpPort {

    private String ip;
    private int port;

    public IpPort(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}

```



[컨버터 정의 - `StringToIpPortConverter`]

```java
package hello.typeconverter.converter;

import hello.typeconverter.type.IpPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

@Slf4j
public class StringToIpPortConverter implements Converter<String, IpPort> {

    @Override
    public IpPort convert(String source) {
        log.info("convert source = {}", source);
        String[] split = source.split(":");
        String ip = split[0];
        int port = Integer.valueOf(split[1]);

        return new IpPort(ip, port);
    }
}
```



[컨버터 정의 - `IpPortToStringConverter`]

```java
package hello.typeconverter.converter;

import hello.typeconverter.type.IpPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

@Slf4j
public class IpPortToStringConverter implements Converter<IpPort, String> {

    @Override
    public String convert(IpPort source) {
        log.info("convert source = {}", source);
        return source.getIp() + ":" + source.getPort();
    }
}
```



[테스트 코드]

```java
package hello.typeconverter.converter;

import hello.typeconverter.type.IpPort;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConverterTest {

    @Test
    void stringToIpPort() {
        StringToIpPortConverter converter = new StringToIpPortConverter();
        String source = "127.0.0.1:8080";
        IpPort result = converter.convert(source);
        assertThat(result).isEqualTo(new IpPort("127.0.0.1", 8080));
    }

    @Test
    void ipPortToString() {
        IpPortToStringConverter converter = new IpPortToStringConverter();
        IpPort ipPort = new IpPort("127.0.0.1", 8080);
        String result = converter.convert(ipPort);
        assertThat(result).isEqualTo("127.0.0.1:8080");
    }
}
```



## 컨버전 서비스 - ConversionService

위와 같이 타입 컨버터를 하나씩 직접 생성해서 타입 변환하는 것은 기존에 사용하던 방식과 유사해서 별로 유용해보이지 않는다. 그래서 <u>스프링은 개별 컨버터를 모아두고, 그것들을 묶어서 편리하게 사용할 수 있는 기능을 제공하는데</u>, 이것이 바로 컨버전 서비스이다.



### ConversionService 인터페이스

컨버전 서비스 인터페이스는 <u>단순히 컨버팅이 가능한지, 확인하는 기능과 컨버팅 기능을 제공한다.</u>

```java
package org.springframework.core.convert;
import org.springframework.lang.Nullable;
   public interface ConversionService {
       boolean canConvert(@Nullable Class<?> sourceType, Class<?> targetType);
       boolean canConvert(@Nullable TypeDescriptor sourceType, TypeDescriptor
   targetType);
       <T> T convert(@Nullable Object source, Class<T> targetType);
       Object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType,
   TypeDescriptor targetType);
}
```



### 예제 - DefaultConversionService

`DefaultConversionService`는 ConversionService 인터페이스를 구현하는데, <u>추가로 컨버터를 등록하는 기능도 제공한다.</u>

```java
package hello.typeconverter.converter;

import hello.typeconverter.type.IpPort;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.support.DefaultConversionService;

import static org.assertj.core.api.Assertions.*;

public class ConversionServiceTest {

    @Test
    void conversionService() {
        // 등록
        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new StringToIntegerConverter());
        conversionService.addConverter(new IntegerToStringConverter());
        conversionService.addConverter(new IpPortToStringConverter());
        conversionService.addConverter(new StringToIpPortConverter());

        // 사용
        assertThat(conversionService.convert("10", Integer.class))
                .isEqualTo(10);

        assertThat(conversionService.convert(10, String.class))
                .isEqualTo("10");

        IpPort ipPort = conversionService.convert("127.0.0.1:8080", IpPort.class);
        assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));

        String ipPortString = conversionService.convert(new IpPort("127.0.0.1", 8080), String.class);
        assertThat(ipPortString).isEqualTo("127.0.0.1:8080");


    }
}
```



### 등록과 사용 분리

* 컨버터를 등록할 땐 타입 컨버터를 명확하게 알아야한다.
* 하지만 컨버터를 사용하는 입장에서는 타입 컨버터를 몰라도 된다.
* <u>따라서 타입 변환을 원하는 사용자는 컨버전 서비스 인터페이스에만 의존하면 된다.</u>



> 인터페이스 분리 원칙 - ISP(Interface Segregation Principle)
>
> <u>인터페이스 분리 원칙은 클라이언트가 자신이 이용하지 않는 메서드에 의존하지 않아야 한다.</u>
> 위의 예제에서처럼 `DefaultConversionService`는 다음 인터페이스를 구현했는데
>
> * ConversionService: 컨버터 사용에 초점
> * ConverterRegistry: 컨버터 등록에 초점
>
> 이렇게 인터페이스를 분리하면 컨버터를 사용하는 클라이언트와,
> 컨버터를 등록하고 관리하는 클라이언트의 관심사를 명확하게 분리할 수 있다.
>
> <u>결과적으로 컨버터를 사용하는 클라이언트는 꼭 필요한 메서드만 알게된다.</u>
> `@RequestParam` 같은 곳에서도 `ConversionService`를 사용해서 타입을 변환한다.





## 스프링에 Converter 적용하기



### 예제 - WebConfig 컨버터 등록 및 사용

[`WebConfig`]

* 스프링은 내부에서 `ConversionService`를 제공한다.
* `WebMvcConfigurer.addFormatters()`를 사용해서 추가하고 싶은 컨버터를 등록하면 된다.
* 이렇게 하면 스프링은 내부에서 사용하는 `ConversionSerivce`에 컨버터를 추가해준다.

```java
package hello.typeconverter;

import hello.typeconverter.converter.IntegerToStringConverter;
import hello.typeconverter.converter.IpPortToStringConverter;
import hello.typeconverter.converter.StringToIntegerConverter;
import hello.typeconverter.converter.StringToIpPortConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToIntegerConverter());
        registry.addConverter(new IntegerToStringConverter());
        registry.addConverter(new IpPortToStringConverter());
        registry.addConverter(new StringToIpPortConverter());

    }
}
```



[`Hellocontroller`]

```java
@GetMapping("/hello-v2")
public String helloV2(@RequestParam Integer data) {
    System.out.println("data = " + data);
    return "ok";
}
```



### 결과

위의 컨트롤러를 실행했을 때 결과는 아래와 같다.

* 스프링은 내부에서 수 많은 기본 컨버터를 제공하게 되는데, <u>컨버터를 추가하면 기존 컨버터 보다 우선순위를 가지게 된다.</u>

```
StringToIntegerConverter   : convert source=10
data = 10
```



### 예제 - 사용자 정의 타입 컨버터 사용하기

```java
@GetMapping("ip-port")
public String ipPort(@RequestParam IpPort ipPort) {
    System.out.println("ipPort.getIp() = " + ipPort.getIp());
    System.out.println("ipPort.getPort() = " + ipPort.getPort());
    return "ok";
}
```



### 결과

<u>`@RequestParam`을 처리하는 `ArgumentResolver`인 `RequestParamMethodArgurmentResolver`에서 `ConversionSerivce`을 사용해서 타입을 변환한다.</u>

결국 내부적으로 `ConversionSerivce`를 사용해서 타입 컨버터를 진행하게 된다.

```
StringToIpPortConverter : convert source=127.0.0.1:8080
ipPort IP = 127.0.0.1
ipPort PORT = 8080
```



## 뷰 템플릿에 컨버터 적용하기

타임리프는 렌더링 시 컨버터를 적용해서 렌더링 하는 방법을 지원한다.
이전까지는 문자를 객체로 변환했다면, <u>이번에는 그 반대로 객체를 문자로 변환하는 작업을 확인할 수 있다.</u>



### 예제

* 타임리프는 `${{...}}`를 사용하면 자동으로 컨버전 서비스를 사용해서 변환된 결과를 출력해준다.
  * `{{number}}` : 뷰 템플릿은 데이터를 문자로 출력한다.
  * `{{ipPort}}`: 뷰 템플릿은 데이터를 문자로 출력한다. IpPort 타입을 String 타입으로 변환해야 하므로 `IpPortToStringConverter`가 적용된다.
* `GET /converter/edit`
  * `th:field`가 자동으로 컨버전 서비스를 적용해주어서 `${{ipPort}}`처럼 적용이 된다. 따라서 IpPort -> String으로 변환된다.
* `POST /converter/edit`
  * `@ModelAttribute`를 사용해서 `String -> IpPort`로 변환된다.

```java
package hello.typeconverter.controller;

import hello.typeconverter.type.IpPort;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ConverterController {

    @GetMapping("/converter-view")
    public String converterView(Model model) {
        model.addAttribute("number", 10000);
        model.addAttribute("ipPort", new IpPort("127.0.0.1", 8080));
        return "converter-view";
    }

    @GetMapping("/converter/edit")
    public String converterForm(Model model) {

        IpPort ipPort = new IpPort("127.0.0.1", 8080);
        Form form = new Form(ipPort);

        model.addAttribute("form", form);
        return "converter-form";
    }

    @PostMapping("/converter/edit")
    public String converterEdit(@ModelAttribute Form form, Model model) {
        IpPort ipPort = form.getIpPort();
        model.addAttribute("ipPort", ipPort);
        return "converter-view";
    }

    @Data
    static class Form {
        private IpPort ipPort;

        public Form(IpPort ipPort) {
            this.ipPort = ipPort;
        }
    }
}
```



[`converter-view`]

```java
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<ul>
    <li>${number}: <span th:text="${number}" ></span></li>
    <li>${{number}}: <span th:text="${{number}}" ></span></li>
    <li>${ipPort}: <span th:text="${ipPort}" ></span></li>
    <li>${{ipPort}}: <span th:text="${{ipPort}}" ></span></li>
</ul>

</body>
</html>
```



[`converter-form`]

* 타임리프의 `th:field`는 id, name, value 뿐만 아니라 컨버전 서비스도 함께 적용한다.

```java
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>

<form th:object="${form}" th:method="post">
    th:field <input type="text" th:field="*{ipPort}"><br/>
    th:value <input type="text" th:value="*{ipPort}">(보여주기 용도)<br/>
    <input type="submit"/>
</form>

</body>
</html>
```
