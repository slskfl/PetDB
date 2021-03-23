package com.example.petdb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

//DB클래스 생성 (생성자(DB생성), onCreate(테이블 생성))
 //자료준비>>DB변환
 //asset(raw, menu 등) DB관련 폴더(활용 DB 복붙>>활용)
 //asset폴더 안에 있는 DB를 해당 프로젝트 database
 //MyDBHelper 클래스는 생성할 때 만드는 클래스
 //이미 존재하는 DB를 읽어들임
public class MainActivity extends AppCompatActivity {
    Spinner spCity, spHName;
    TextView tvResult;
    SQLiteDatabase sqlDB;
    ArrayAdapter<String> adapterC, adapterH;
    ArrayList<String> siData, nameData;
    String result;
    Button btnMap;
    double lat, lng;// 동물병원의 위도와 경도
    String name, tel;// 동물병원의 이름과 전화번호를 전달
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spCity=findViewById(R.id.spCity);
        spHName=findViewById(R.id.spHName);
        tvResult=findViewById(R.id.tvResult);
        btnMap=findViewById(R.id.btnMap);
        siData=new ArrayList<String>();
        nameData=new ArrayList<String>();
        //파일 처리할 경우 예외처리 필수
        try {
            boolean check= isCheckDB(this);
            if(!check){
                copyDB(this);
            }
            sqlDB=SQLiteDatabase.openDatabase("/data/data/com.example.petdb/databases/petHDB.db",
                    null, SQLiteDatabase.OPEN_READONLY);
            Cursor cursor;
            cursor=sqlDB.rawQuery("SELECT distinct(hs) FROM petHTBL;", null);
            while(cursor.moveToNext()){
                siData.add(cursor.getString(0));
            }
            adapterC=new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, siData );
            spCity.setAdapter(adapterC);
            cursor.close();
            spCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    nameData.clear();
                    Cursor cursor1;
                    cursor1=sqlDB.rawQuery("SELECT name FROM petHTBL WHERE hs='"+spCity.getSelectedItem().toString()+"';",null );
                    while(cursor1.moveToNext()){
                        nameData.add(cursor1.getString(0));
                    }
                    adapterH=new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item,nameData);
                    spHName.setAdapter(adapterH);
                    cursor1.close();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spHName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Cursor cursor2;
                    cursor2=sqlDB.rawQuery("SELECT * FROM petHTBL " +
                            "WHERE  hs='"+spCity.getSelectedItem().toString()+
                            "'AND name='"+spHName.getSelectedItem().toString()+"';",null);
                    if(cursor2.moveToFirst()){
                        name=cursor2.getString(1);
                        tel=cursor2.getString(5);
                        result="동물병원이름 : " + name +"\n";
                        if(cursor2.getString(3).equals("정상")) {
                            result += "개업일 : " + cursor2.getString(2) + "\n";
                        }else{
                            result += "개업일 : " + cursor2.getString(2)
                                    + "("+cursor2.getString(3)+":"+cursor2.getString(4)+")"+"\n";
                        }
                        result+="전화번호 : " + tel +"\n";
                        result+="우편번호 : " + cursor2.getString(6)+"\n";
                        result+="주소 : " + cursor2.getString(7)+"\n";
                        tvResult.setText(result);
                        cursor2.close();
                        btnMap.setEnabled(true);
                        lat=cursor2.getDouble(8);
                        lng=cursor2.getDouble(9);
                    }else{
                        showToast("자료가 존재하지 않습니다.");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch (Exception e){
            showToast("복사 중에 에러가 발생했습니다.");
        }
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("Name", name);
                intent.putExtra("Tel", tel);
                intent.putExtra("Lat", lat);
                intent.putExtra("Lng", lng);
                startActivity(intent);
            }
        });
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