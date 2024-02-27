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



