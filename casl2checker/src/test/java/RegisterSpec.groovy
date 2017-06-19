import jp.ac.fukuoka_u.tl.casl2emu.Casl2Register
import spock.lang.Specification

/**
 * Created by furusho on 2017/05/19.
 */
class RegisterSpec extends Specification{
    def "getgp check"(){
        setup:
        def reg=new Casl2Register();
        reg.setGr((char)v,0)

        expect:
        reg.getGr()[0]==result

        where:
        v||result
        0x0003||0x0003

    }
}
