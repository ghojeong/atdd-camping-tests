package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class SampleSteps {

    @When("{string}에 요청을 보낸다")
    public void 요청을보낸다(String url) {
        System.out.println(url + "에 요청을 보냈다");
    }

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        System.out.println("성공 응답을 받았다");
    }
}


