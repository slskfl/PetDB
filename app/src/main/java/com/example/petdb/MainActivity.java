package com.example.petdb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//DB클래스 생성 (생성자(DB생성), onCreate(테이블 생성))
 //자료준비>>DB변환
 //asset(raw, menu 등) DB관련 폴더(활용 DB 복붙>>활용)
 //asset폴더 안에 있는 DB를 해당 프로젝트 database
 //MyDBHelper 클래스는 생성할 때 만드는 클래스
 //이미 존재하는 DB를 읽어들임
public class MainActivity extends AppCompatActivity {
    SQLiteDatabase sqlDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //파일 처리할 경우 예외처리 필수
        try {
            boolean check= isCheckDB(this);
            if(!check){
                copyDB(this);
            }
            sqlDB=SQLiteDatabase.openDatabase("/data/data/com.example.petdb/databases/petHDB.db",
                    null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception e){
            showToast("복사 중에 에러가 발생했습니다.");
        }
    }
    void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
    //DB체크 메서드
     public boolean isCheckDB(Context context){
        String filePath="/data/data/com.example.petdb/databases/petHDB.db";
        File file=new File(filePath);
        long newDBSize=0;
        long oldDBSize=file.length(); // 이전 DB의 파일 크기
         AssetManager manager=context.getAssets(); // asset폴더의 접근하는 명령어
         try{
             InputStream inputStream=manager.open("petHDB.db");
             newDBSize=inputStream.available(); //읽어온 파일의 크기만큼 넣는다.

         } catch (IOException e){
             showToast("파일을 복사할 수 없습니다.");
         }
         if(file.exists()){
             if(newDBSize != oldDBSize){
                 return false;
             }else{
                 return true;
             }
         } else {
             //파일이 존재하지 않을 경우
             return false;
         }
     }
     //DB복사 메서드
     public void copyDB(Context context){
        AssetManager manager=context.getAssets();
        String dbFolderPath="/data/data/com.example.petdb/databases";
        String filePath="/data/data/com.example.petdb/databases/petHDB.db";
        File folder=new File(dbFolderPath);
        File file=new File(filePath);
        FileOutputStream fileOs=null;
        BufferedOutputStream bufferOs=null;
        try{
             InputStream inputS=manager.open("petHDB.db");
             BufferedInputStream bufferIs=new BufferedInputStream(inputS);
             if(!folder.exists()){
                 //파일 폴더가 존재하지 않을 경우 (초기에는 존재하지 않음) 새로운 폴더 만들기
                 folder.mkdir();
             }
             if(file.exists()){
                 //파일이 존재하는 경우
                 file.delete();
                 file.createNewFile(); // 파일 새로 만들기
             }
             fileOs=new FileOutputStream(file);
             bufferOs=new BufferedOutputStream(fileOs);
             int read=-1;
             byte buffer[]=new byte[bufferIs.available()];
             while ((read=bufferIs.read(buffer, 0, bufferIs.available())) != -1 ){
                 // 파일 내용이 읽어올 게 없을 경우
                 bufferOs.write(buffer,0,read);
             }
             bufferOs.flush();
             bufferOs.close();
             bufferIs.close();
             fileOs.close();
             inputS.close();
        } catch (IOException e){
             showToast("파일을 복사할 수 없습니다");
        }
     }
}