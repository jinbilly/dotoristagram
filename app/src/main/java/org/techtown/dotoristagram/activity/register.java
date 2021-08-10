package org.techtown.dotoristagram.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.techtown.dotoristagram.R;
import org.techtown.dotoristagram.activity.friendsFeed;
import org.techtown.dotoristagram.activity.login;
import org.techtown.dotoristagram.retrofit.RetrofitAPI;
import org.techtown.dotoristagram.retrofit.UserInfo;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class register extends AppCompatActivity {
    String code="0";
    boolean code_confirm;//코드 인증 여부를 위해
    boolean Id_availability;//ID중복 유효성 체크
    boolean PW_availability;//PW와 PW확인 일치 및 유효성 체크


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

         code_confirm = false;//코드 인증 여부를 위해
         Id_availability =false;//ID중복 유효성 체크
         PW_availability =false;//PW와 PW확인 일치 및 유효성 체크

        /*레트로핏 객체 생성*/
        Gson gson  = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://54.248.192.133")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();


        EditText ID = findViewById(R.id.ID);
        TextView checkId = findViewById(R.id.checkId);
        EditText PW = findViewById(R.id.PW);
        EditText pwConfirm = findViewById(R.id.pwConfirm);
        TextView checkPw = findViewById(R.id.checkPw);
        EditText Name = findViewById(R.id.Name);
        EditText PhoneNum = findViewById(R.id.PhoneNum);
        Button btn_register = findViewById(R.id.btn_register);
        //Button btn_test = findViewById(R.id.btn_test);
        Button btn_confirm = findViewById(R.id.btn_confirm);
        Button confirm = findViewById(R.id.confirm);
        EditText et_code  = findViewById(R.id.code);
        CheckBox checkBoxALL = findViewById(R.id.checkBox);
        CheckBox checkBox1 = findViewById(R.id.checkBox2);
        CheckBox checkBox2 = findViewById(R.id.checkBox3);
        TextView agreement1 = findViewById(R.id.agreement1);
        TextView agreement2 = findViewById(R.id.agreement2);


        PhoneNum.addTextChangedListener(new PhoneNumberFormattingTextWatcher());//자동으로 - 휴대폰 번호 하이픈 생기게 해주기

        //휴대폰 번호 인증 버튼
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //핸드폰번호 유효성 검사
                if(!Pattern.matches("^010-\\d{4}-\\d{4}$", PhoneNum.getText().toString()))
                {
                    Toast.makeText(getApplicationContext(),"올바른 핸드폰 번호가 아닙니다.",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"인증번호가 발송되었습니다.",Toast.LENGTH_LONG).show();  //인증번호 발송 토스트메시지 띄워주기
                    String phoneNum = PhoneNum.getText().toString().replace("-","");
                    RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
                    Call<String> comment = retrofitAPI.sendSms(phoneNum);//폰번호를 가져와서 인터페이스에 넣어주기
                    comment.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            code = response.body();//코드를 넣어주기
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    });
                }

            }
        });

        //인증번호 확인 버튼
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(code.equals(et_code.getText().toString())){//인증코드가 일치 할 경우
                    Toast.makeText(getApplicationContext(),"인증되었습니다.",Toast.LENGTH_LONG).show();
                    code_confirm=true;
                }else{
                    Toast.makeText(getApplicationContext(),"인증번호가 틀렸습니다.",Toast.LENGTH_LONG).show();
                    code_confirm=false;
                }
            }
        });

/*

        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
*/

        /*아이디 입력 빈칸에 마우스 커서가 있고 없고에 따른 이벤트*/
        ID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    checkId.setText("");
                }else{
                    RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
                    Call<String> comment = retrofitAPI.IdCheck(ID.getText().toString());//Id 값을 가져와서 인터페이스에 넣어주기

                    comment.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            String answer = response.body();
                            if(answer.equals("사용가능한 아이디 입니다.")){
                                checkId.setText(answer);
                                checkId.setTextColor(0xFF1E88E5);
                                Id_availability = true;
                            }else{
                                if(!ID.getText().toString().equals("")){    //빈칸이 아닐때
                                    checkId.setText(answer);
                                    checkId.setTextColor(0xFFE51E1E);
                                    Id_availability =false;
                                }
                            }
                        }
                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.d("실패", "낑");
                        }
                    });
                }

            }
        });

        /*비밀번호와 비밀번호 확인이 맞는지 체크, 비밀번호 확인 칸에서 포커스 일어날때*/
        pwConfirm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    checkPw.setText("");
                }else{
                    if(PW.getText().toString().equals(pwConfirm.getText().toString())){//비번이랑 비번확인랑 같는지 보기
                        checkPw.setText("");
                        PW_availability=true;
                    }else{
                        checkPw.setText("비밀번호와 비밀번호 확인이 틀립니다.");
                        checkPw.setTextColor(0xFFE51E1E);
                        PW_availability =false;
                    }

                }
            }
        });

        //전체약관에 동의 checkbox부분
        checkBoxALL.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(checkBoxALL.isChecked()){    //전체동의가 체크 되어 있으면
                    checkBox1.setChecked(true);
                    checkBox2.setChecked(true);
                }else{
                    checkBox1.setChecked(false);
                    checkBox2.setChecked(false);
                }

            }
        });

        //약관동의 클릭 이벤트

        agreement1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(register.this);
                dlg.setTitle("Dotoristagram 이용약관"); //제목
                dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dlg.setMessage("이용 약관\n" +
                        "Dotoristagram에 오신 것을 환영합니다!\n" +
                        "\n" +
                        "당사가 명시적으로 별도의 약관(본 약관이 아님)이 적용된다고 밝히지 않는 한 본 이용 약관(또는 '약관')이 귀하의 Dotoristagram 사용에 적용되며 아래 설명된 Dotoristagram 서비스('서비스')에 대한 정보를 제공합니다. 귀하가 Dotoristagram 계정을 만들거나 Dotoristagram을 이용하는 경우, 귀하는 본 약관에 동의하는 것으로 간주됩니다. Facebook 서비스 약관은 이 서비스에 적용되지 않습니다.\n" +
                        "\n" +
                        "Dotoristagram 서비스는 Facebook, Inc.에서 귀하에게 제공하는 Facebook 제품 중 하나입니다. 따라서 본 이용 약관은 귀하와 Facebook, Inc. 사이의 계약에 해당됩니다.\n" +
                        "\n" +
                        "Dotoristagram 서비스\n" +
                        "당사는 귀하에게 Dotoristagram 서비스를 제공하는 것에 동의합니다. 서비스에는 당사가 Dotoristagram의 사명을 실현하기 위해 제공하는 모든 Dotoristagram 제품, 기능, 앱, 서비스, 기술 및 소프트웨어가 포함됩니다. 귀하가 관심 있는 사람들과 항목을 더 가까이 접할 수 있도록 당사의 서비스는 다음과 같은 요소로 구성됩니다:\n" +
                        "\n" +
                        "창작, 연결, 커뮤니케이션, 발견 및 공유를 위한 맞춤화된 기회를 제공합니다.\n" +
                        "사람들은 다양합니다. 당사는 귀하가 실제로 관심을 가지는 경험을 공유함으로써 관계를 강화하길 원합니다. 따라서 당사는 귀하 및 다른 사람들이 관심 있는 사람과 사항을 파악하는 시스템을 구축하고 파악한 정보를 이용하여 귀하가 중요한 경험을 창작하고, 발견하며, 동참하고, 공유하도록 합니다. 그 일환으로, 귀하 및 다른 사람들이 Dotoristagram 안팎에서 하는 활동에 기초하여 귀하가 관심을 가질 만한 콘텐츠, 기능, 제안 및 계정을 더 잘 보여드리고, 귀하에게 Dotoristagram을 경험하는 방법을 제안하기도 합니다.\n" +
                        "긍정적이고 포괄적이며 안전한 환경을 조성합니다.\n" +
                        "당사는 당사 커뮤니티 회원들이 도움을 필요로 한다고 생각하는 경우를 포함하여 회원들의 경험을 긍정적이고 폭넓게 만들도록 도구를 개발하고 사용하고, 자원을 제공합니다. 또한 당사는 당사 약관 및 정책의 남용 및 위반을 포함하여 유해하고 사기적인 행위를 방지하기 위한 팀과 시스템을 갖추고 있습니다. 당사는 당사 플랫폼의 보안을 유지하기 위해 당사가 보유한 모든 정보(귀하의 정보 포함)를 이용합니다. 또한 당사는 오용 또는 유해 콘텐츠에 관한 정보를 다른 Facebook 계열사나 사법당국과 공유할 수 있습니다. 데이터 정책에서 자세히 알아보세요.\n" +
                        "성장하고 있는 당사 커뮤니티에 지속적으로 서비스를 제공하는 데 도움이 되는 기술을 개발하고 사용합니다.\n" +
                        "성장하고 있는 당사 커뮤니티를 위해 정보를 구성하고 분석하는 것이 당사 서비스의 핵심입니다. 당사 서비스에서 큰 부분을 차지하는 것은 광범위한 글로벌 커뮤니티를 위하여 엄청나게 큰 규모로 당사 서비스를 맞춤화하고, 보호하며, 향상시키는 데 도움이 되는 첨단 기술을 개발하고 사용하는 것입니다. 인공 지능 및 머신 러닝과 같은 기술은 당사가 당사 서비스 전체에 복잡한 프로세스를 적용할 수 있게 합니다. 자동화 기술 또한 당사 서비스의 기능성과 무결성 보장에 기여합니다.\n" +
                        "Facebook 계열사 제품 전반에 걸쳐 일관되고 원활한 경험을 제공합니다.\n" +
                        "Dotoristagram은 Facebook 계열사 중 하나로서, Facebook 계열사들은 보다 우수하고 안전하며 보안이 강화된 서비스를 제공하기 위해 기술, 시스템, 인사이트 및 정보를 공유합니다. 그 정보에는 당사가 보유하는 귀하에 대한 정보가 포함되며, 자세한 내용은 데이터 정책에서 알아보세요. 또한 귀하가 사용하는 Facebook 계열사 제품과 상호 작용할 수 있는 방법을 제공하며, 귀하가 Facebook 계열사 제품 전반에서 일관적이고 원활한 경험을 얻을 수 있도록 하는 시스템을 설계했습니다.\n" +
                        "당사 서비스 이용에 대한 보장.\n" +
                        "글로벌 서비스를 운영하기 위해 당사는 귀하의 거주 국가 이외 지역을 비롯하여 전 세계에 있는 시스템에 정보를 저장하고 전송해야 합니다. 서비스를 제공하기 위해서는 본 글로벌 인프라의 이용이 필수적입니다 이 인프라는 Facebook, Inc., Facebook Ireland Limited 또는 그 계열사가 소유하거나 운영할 수 있습니다.\n" +
                        "귀하가 관심 있는 방식으로 귀하와 브랜드, 제품 및 서비스를 연결합니다.\n" +
                        "당사는 귀하에게 의미 있다고 생각되는 광고, 제안 및 기타 홍보 콘텐츠를 보여드리기 위해 Dotoristagram 및 다른 Facebook 계열사 제품의 정보 외에도 제삼자의 정보를 이용합니다. 그리고 귀하의 Dotoristagram에서의 경험과 콘텐츠를 연관시키려 노력합니다.\n" +
                        "연구 및 혁신.\n" +
                        "당사는 보유하고 있는 정보를 이용하여 당사 서비스를 연구하고, 다른 사람들과 협력하여 보다 나은 당사 서비스를 만들고 당사 커뮤니티의 복지에 기여하기 위한 연구를 수행합니다.\n" +
                        "\n" +
                        "Dotoristagram 서비스의 재정 조달 방식\n" +
                        "Dotoristagram의 사용료를 지불하지 않는 대신, 본 약관이 적용되는 서비스를 사용함으로써, 귀하는 사업체 및 단체들이 홍보를 위해 당사에 비용을 지불한 광고를 Facebook 및 회사 제품 내부와 외부에서 보게 될 수 있다는 것을 인정합니다. 당사는 귀하와 관련성이 높은 광고를 보여드리기 위해 귀하의 활동 및 관심사에 대한 정보 등 귀하의 개인 정보를 활용합니다.\n" +
                        "\n" +
                        "당사는 귀하께 귀하와 관련 있고 유용한 광고를 제시하되, 광고주에게 귀하의 신원을 알리지는 않습니다. 당사는 귀하의 개인정보를 판매하지 않습니다. 광고주는 당사에 사업 목적, 목표한 광고 노출 대상 등을 알려줄 수 있습니다. 당사는 그 후 관심이 있을 만한 사람에게 광고를 노출합니다.\n" +
                        "\n" +
                        "당사는 또한 사람들이 Dotoristagram 내부와 외부에서 광고주의 콘텐츠와 어떻게 상호작용하고 있는지 광고주들이 이해할 수 있도록 광고주들에게 광고의 성과 보고서를 제공합니다. 예를 들어 광고주가 광고의 타겟을 더 잘 이해할 수 있도록 당사는 일반적 인구 통계 정보 및 관심사 정보를 광고주에게 제공할 수 있습니다. 당사는 귀하가 구체적으로 허락하지 않는 한 귀하를 직접적으로 식별할 수 있는 정보(이름, 또는 이메일 주소와 같이 그 정보만으로 귀하께 연락을 취하는 데 사용될 수 있거나 귀하를 식별할 수 있는 정보)를 공유하지 않습니다. 여기에서 Dotoristagram 광고의 작동 방식에 대해 자세히 알아보세요.\n" +
                        "\n" +
                        "Dotoristagram 콘텐츠에 언급되어 있는 비즈니스 파트너와의 상업적 관계를 기반으로 제품 또는 서비스를 홍보하는 계정의 소유자가 게시한 브랜디드 콘텐츠가 표시될 수 있습니다. 관련해서는 여기에서 자세히 알아보세요.\n" +
                        "\n" +
                        "\n" +
                        "데이터 정책\n" +
                        "당사 서비스를 제공하기 위해서 귀하의 정보를 수집하고 이용해야 합니다. 데이터 정책은 Facebook 제품에서 당사가 정보를 수집, 이용하고 공유하는 방법에 대해 설명합니다. 또한 Dotoristagram 개인정보 보호 및 보안 설정을 포함해 귀하의 정보를 관리할 수 있는 여러 방법에 대해 설명합니다. Dotoristagram을 이용하려면 데이터 정책에 동의해야 합니다.\n" +
                        "\n" +
                        "\n" +
                        "귀하의 약속\n" +
                        "서비스 제공에 대한 당사의 약속에 대해 귀하는 다음과 같이 약속을 해주셔야 합니다.\n" +
                        "Dotoristagram을 이용할 수 있는 주체. 당사는 Dotoristagram 서비스가 가능한 개방적이고 포괄적인 서비스가 되기를 원하지만, 또한 본 서비스가 안전하고, 보안성을 갖추며, 법을 준수하는 서비스가 되기를 원합니다. 따라서 귀하가 Dotoristagram 커뮤니티에 참여하려면 몇 가지 제한 사항을 준수해야 합니다.\n" +
                        "만 14세 이상이어야 합니다.\n" +
                        "관련 법률에 따라 Dotoristagram 서비스를 받는 것이 금지된 사람 또는 관련 제재 대상 명단에 있어 결제 관련 Dotoristagram 서비스를 이용하는 것이 금지된 사람이 아니어야 합니다.\n" +
                        "과거에 당사가 귀하의 계정을 법률 또는 Dotoristagram 정책 위반을 이유로 하여 비활성화한 적이 없어야 합니다.\n" +
                        "유죄 확정판결을 받은 성범죄자가 아니어야 합니다.\n" +
                        "Dotoristagram을 사용할 수 없는 경우. 광범위한 커뮤니티에 안전하고 개방적인 Dotoristagram 서비스가 제공되기 위해서는 우리 모두가 각자의 본분을 다해야 합니다.\n" +
                        "다른 사람을 사칭하거나 부정확한 정보를 제공하면 안 됩니다.\n" +
                        "Dotoristagram에서 귀하의 신원을 공개할 필요는 없지만, 귀하는 당사에 정확한 최신 정보(등록 정보 포함)를 제공해야 하며 개인 정보를 제공해야 할 수도 있습니다. 또한 본인이 아닌 다른 사람이나 단체를 사칭해서는 안 되며, 다른 사람의 명시적인 허락 없이 다른 사람의 이름으로 계정을 만들 수 없습니다.\n" +
                        "불법적이거나, 오해의 소지가 있거나, 사기적인 행위 또는 불법적이거나 허가받지 않은 목적을 위한 어떠한 행위도 하면 안 됩니다.\n" +
                        "특히 귀하는 Dotoristagram 커뮤니티 가이드라인, Facebook 플랫폼 이용 약관 및 개발자 정책, 음악 가이드라인을 포함하여 본 약관 또는 당사 정책을 위반할 수 없으며, 다른 사람들이 위반하도록 돕거나 조장해서도 안 됩니다.\n" +
                        "브랜디드 콘텐츠를 게시하는 경우 귀하는 당사의 브랜디드 콘텐츠 정책을 준수해야 하며, 이에 따라 당사의 브랜디드 콘텐츠 도구를 사용해야 합니다. 행동 또는 콘텐츠를 신고하는 방법은 고객 센터에서 알아보세요.\n" +
                        "귀하는 서비스의 정상적인 운영을 방해하거나 지장을 주는 어떠한 행동도 해서는 안 됩니다.\n" +
                        "여기에는 사기성이 짙거나 근거 없는 신고 또는 재고 요청 등 신고, 이의 제기 또는 재고 요청 채널의 오용이 포함됩니다.\n" +
                        "허가받지 않은 방법으로 계정을 만들거나 정보에 접근하거나 정보를 수집하려 해서는 안 됩니다.\n" +
                        "여기에는 당사의 명시적 허락 없이 자동화된 방법으로 계정을 만들거나 정보를 수집하는 것이 포함됩니다.\n" +
                        "귀하는 당사 또는 당사 서비스를 통해 확보한 계정이나 정보를 판매하거나, 라이선스를 부여하거나, 구매해서는 안 됩니다.\n" +
                        "여기에는 귀하 계정의 일부(사용자 이름 포함) 또는 전부에 대한 구매, 판매 또는 양도가 포함되며, 다른 사용자의 로그인 정보나 신분증을 요청, 수집 또는 사용하는 행위 및 Dotoristagram 사용자 이름, 비밀번호, 부적절한 액세스 토큰에 대한 요청 또는 수집 행위가 포함됩니다.\n" +
                        "귀하는 다른 사람의 사생활 정보 또는 기밀 정보를 허가 없이 게시할 수 없으며 지적 재산권을 포함하여 다른 사람의 권리를 침해하는 행위(예: 저작권 침해, 상표권 침해, 모조품, 해적판)를 하여서는 안 됩니다.\n" +
                        "귀하는 관련법의 저작권 및 관련 권리에 관한 예외 또는 제한 사항에 따라 다른 사람의 저작물을 사용할 수 있습니다. 귀하는 콘텐츠를 게시하거나 공유하기 위한 모든 권리를 소유하고 있거나 얻었음을 진술합니다. 여기에서 귀하의 지적 재산권을 침해한다고 생각되는 콘텐츠를 신고하는 방법을 포함한 자세한 내용을 알아보세요.\n" +
                        "귀하는 당사의 제품 또는 그 구성 요소를 수정, 변환할 수 없으며, 그 파생 저작물을 제작하거나 역설계를 수행해서는 안 됩니다.\n" +
                        "당사의 사전 서면 동의 없이 귀하의 사용자 이름에 도메인 이름이나 URL을 사용하면 안 됩니다.\n" +
                        "귀하가 당사에 부여한 권한. 본 계약의 일부로서, 귀하는 또한 당사가 귀하에게 서비스를 제공하기 위해 필요로 하는 권한을 당사에 부여합니다.\n" +
                        "당사가 귀하의 콘텐츠에 대한 소유권을 주장하지 않는 것과는 별개로, 귀하는 당사에 귀하 콘텐츠 이용을 허용하는 라이선스를 부여합니다.\n" +
                        "귀하의 콘텐츠에 대한 귀하의 권리에 있어 변경되는 사항은 없습니다. 당사는 서비스에 또는 서비스를 통해 귀하가 게시하는 콘텐츠에 대한 소유권을 주장하지 않으며 귀하는 원하는 곳 어디에서나 다른 사람들과 자유롭게 콘텐츠를 공유할 수 있습니다. 그러나 당사가 서비스를 제공하기 위해서는 귀하로부터 특정 법적 권한('라이선스')을 부여받아야 합니다. 귀하가 지적 재산권(사진 또는 동영상 등)이 적용되는 콘텐츠 또는 당사 서비스와 관련된 콘텐츠를 공유, 게시 또는 업로드할 때, 귀하는 귀하의 콘텐츠를 전 세계적으로 호스팅, 사용, 배포, 수정, 실행, 복사, 공개적으로 수행 또는 표시, 번역 및 그 파생 저작물을 생성할 수 있는 비독점적이고 양도 가능하며 2차 라이선스를 가질 수 있고 사용료가 없는 라이선스를 당사에 부여합니다(귀하의 개인정보 처리방침 및 애플리케이션 설정과 일치함). 이러한 라이선스는 당사 서비스에 접속하고 이를 이용하는 귀하 및 다른 사람들에게 당사 서비스 이용을 가능하게 하기 위한 것입니다. 귀하는 귀하의 콘텐츠를 개별적으로 삭제하거나, 계정 삭제를 통해 한 번에 모두 삭제할 수 있습니다. 당사의 정보 사용 방법과 콘텐츠 관리 또는 삭제 방법에 관한 자세한 내용을 확인하시려면, 데이터 정책을 살펴보시고 Dotoristagram 고객 센터를 방문하세요.\n" +
                        "귀하의 사용자 이름, 프로필 사진 및 귀하의 관계와 행동에 관한 정보를 계정, 광고 및 홍보 콘텐츠에 이용할 수 있는 권한.\n" +
                        "귀하는 당사에 귀하의 사용자 이름, 프로필 사진 및 귀하의 활동(예: '좋아요') 또는 관계(예: '팔로우')에 관한 정보를 Facebook 제품 전반에 표시되는 계정, 광고, 제안 및 귀하가 팔로우하거나 참여하는 기타 홍보 콘텐츠와 함께 또는 이와 관련하여 표시할 수 있는 권한을 무상으로 부여합니다. 예를 들어, 당사는 귀하가 Dotoristagram에 유료로 광고를 게재하는 브랜드의 홍보 게시물을 좋아했다는 점을 표시할 수 있습니다. 다른 콘텐츠에서의 행동 및 다른 계정에 대한 팔로우와 마찬가지로, 홍보 콘텐츠에서의 행동 및 홍보 계정에 대한 팔로우도 해당 콘텐츠나 팔로우를 볼 수 있는 권한이 있는 사람들에게만 표시됩니다. 당사는 귀하의 광고 설정 또한 존중합니다. 여기에서 귀하의 광고 설정에 대해 자세히 알아보실 수 있습니다.\n" +
                        "귀하는 당사가 귀하의 기기에서 서비스 업데이트를 다운로드하고 설치할 수 있다는 것에 동의합니다.");

                dlg.show();
            }
        });


        agreement2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(register.this);
                dlg.setTitle("Dotoristagram 개인정보처리방침"); //제목
                dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dlg.setMessage("(주)Dotoristagram 은 아래의 목적으로 개인정보를 수집 및 이용하며, 회원의 개인정보를 안전하게 취급하는데 최선을 다하고 있습니다.\n" +
                        "\n" +
                        "1. 수집목적\n" +
                        "- 서비스 제공에 관한 계약 이행 및 서비스 제공에 따른 요금정산\n" +
                        "- 민원사무 처리\n" +
                        "- 재화 또는 서비스 제공\n" +
                        "- 추천인 아이디 활용\n" +
                        "\n" +
                        "2. 수집항목\n" +
                        "회원가입 시\n" +
                        "(필수) 아이디, 비밀번호, 이름, 성별,  휴대폰번호, 본인인증결과\n" +
                        "\n" +
                        "본인인증 시\n" +
                        "(필수) 이름, 휴대폰번호, 본인인증결과\n" +
                        "\n" +
                        "3. 보유기간\n" +
                        "수집된 정보는 회원탈퇴 요청 5일 후 지체없이 파기됩니다. 다만 내부 방침에 의해 서비스 부정이용기록은 부정 가입 및 이용 방지를 위하여 회원 탈퇴 시점으로부터 최대 1년간 보관 후 파기하며, 관계법령에 의해 보관해야 하는 정보는 법령이 정한 기간 동안 보관한 후 파기합니다. 서비스 제공을 위해 필요한 최소한의 개인정보이므로 동의를 해 주셔야 서비스 이용이 가능합니다.\n"); // 메시지


                dlg.show();
            }
        });




        //최종 가입하기 버튼
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if("".equals(ID.getText().toString())){//아이디가 빈칸이면 걸러주기
                    Toast.makeText(getApplicationContext(),"아이디를 입력 해주세요",Toast.LENGTH_LONG).show();
                    return;
                }
                if("".equals(PW.getText().toString())){//비밀번호가 빈칸이면 걸러주기
                    Toast.makeText(getApplicationContext(),"비밀번호를 입력 해주세요",Toast.LENGTH_LONG).show();
                    return;
                }
                if("".equals(pwConfirm.getText().toString())){//비밀번호 확인이 빈칸이면 걸러주기
                    Toast.makeText(getApplicationContext(),"비밀번호 확인을 입력 해주세요",Toast.LENGTH_LONG).show();
                    return;
                }
                if("".equals(PhoneNum.getText().toString())){//핸드폰 번호가 빈칸이면 걸러주기
                    Toast.makeText(getApplicationContext(),"핸드폰 번호를 입력 해주세요",Toast.LENGTH_LONG).show();
                    return;
                }

                if(!Id_availability){//아이디가 중복일 때
                    Toast.makeText(getApplicationContext(),"아이디가 중복되어 다른 아이디를 사용해 주세요",Toast.LENGTH_LONG).show();
                    return;
                }

                if(!PW_availability){//비밀번호와 비밀번호 확인이 다를때
                    Toast.makeText(getApplicationContext(),"비밀번호와 비밀번호 확인이 다릅니다.",Toast.LENGTH_LONG).show();
                    return;
                }

                if(!code_confirm){//인증코드 인증을 제대로 하지 않았을때
                    Toast.makeText(getApplicationContext(),"인증번호 확인이 제대로 되지 않았습니다.",Toast.LENGTH_LONG).show();
                    return;
                }


                RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
                UserInfo userInfo = new UserInfo(ID.getText().toString(),PW.getText().toString(),Name.getText().toString(),PhoneNum.getText().toString());
                Call<String> call = retrofitAPI.registerData(userInfo);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        Log.d("userId", response.body());
                        Log.d("성공","zz");
                        Log.d("성공",call.toString());

                        Intent intent = new Intent(getApplicationContext(), login.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d("아실패",call.toString());
                        Log.d("아실패",t.getMessage());
                    }
                });


            }
        });




    }
}
