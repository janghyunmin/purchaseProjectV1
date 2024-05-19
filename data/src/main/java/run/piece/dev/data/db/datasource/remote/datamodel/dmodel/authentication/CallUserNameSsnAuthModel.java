package run.piece.dev.data.db.datasource.remote.datamodel.dmodel.authentication;

/**
 * packageName    : run.piece.dev.data.datamodel.dmodel.authentication
 * fileName       : CallUserAuthModel
 * author         : piecejhm
 * date           : 2023/02/07
 * description    : 실명인증 v2 Auth Model
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023/02/07        piecejhm       최초 생성
 */


public class CallUserNameSsnAuthModel {
    private String name;
    private String ssn;
    private String agree1;
    private String agree2;

    private String deviceId;

    public CallUserNameSsnAuthModel(String name, String ssn, String agree1, String agree2, String deviceId) {
        this.name = name;
        this.ssn = ssn;
        this.agree1 = agree1;
        this.agree2 = agree2;
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getAgree1() {
        return agree1;
    }

    public void setAgree1(String agree1) {
        this.agree1 = agree1;
    }

    public String getAgree2() {
        return agree2;
    }

    public void setAgree2(String agree2) {
        this.agree2 = agree2;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
