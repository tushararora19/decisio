package com.decisio.models;

import java.util.List;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("ManagerQues")
public class ManagerQuestions extends ParseObject {

    private int locId; 
    private String ques1;
    private String ques2;
    private String ques3;
    
    public ManagerQuestions(){
        
    }
    
    public ManagerQuestions(int id, List<String> questions){
        locId = id;
        ques1 = questions.get(0);
        ques2 = questions.get(1);
        ques3 = questions.get(2);
    }
    
    public String getQues1() {
        return ques1;
    }
    
    public void setLocId(int locId) {
        this.locId = locId;
    }

    public void setQues1(String ques1) {
        this.ques1 = ques1;
    }
    public String getQues2() {
        return ques2;
    }
    public void setQues2(String ques2) {
        this.ques2 = ques2;
    }
    public String getQues3() {
        return ques3;
    }
    public void setQues3(String ques3) {
        this.ques3 = ques3;
    }

    public int getLocId() {
        return locId;
    }
    
}
