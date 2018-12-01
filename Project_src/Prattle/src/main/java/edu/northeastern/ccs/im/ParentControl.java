package edu.northeastern.ccs.im;


import java.util.Arrays;


public class ParentControl {
    /**
     * List containing the bad words
     */

    private String[] words;

    private static ParentControl instance;

    /**
     * Method to read the data from the text file and create a list of bad words
     */
    private ParentControl() {
            words = content.split("\n");
    }

    public static ParentControl getInstance() {
        if (instance == null) {
            instance = new ParentControl();
        }
        return instance;
    }

    /**
     * Filters out the profanities with stars
     *
     * @param message string which is entered by the user
     * @return a filtered message with stars replacing the bad words
     */
    public String filterBadWords(final String message) {
        StringBuilder sb = new StringBuilder();
        String[] words = message.split("\\s");
        for (String word : words) {
            String cuss = word.toLowerCase().replaceAll("\\W+", "");
            checkPatterns(word, cuss, sb);
            sb.append(' ');
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * This method checks for the cuss word in the dictionary and replaces it with *
     *
     * @param word          the word detected in the message
     * @param cuss          the word matched to the cuss word dictionary
     * @param sb StringBuffer for concatenating the message
     */
    private void checkPatterns(final String word, final String cuss, final StringBuilder sb) {
        for (String pattern : words) {
            if (cuss.matches(pattern)) {
                char[] stars = new char[cuss.length()];
                Arrays.fill(stars, '*');
                String rep = new String(stars);
                sb.append(word.replaceAll("\\w+", rep));
                return;
            }
        }
        sb.append(word);
    }

    private String content = "2 girls 1 cup\n"+
            "2g1c\n"+
            "4r5e\n"+
            "5h1t\n"+
            "5hit\n"+
            "a$$\n"+
            "a$$hole\n"+
            "a_s_s\n"+
            "a2m\n"+
            "a54\n"+
            "a55\n"+
            "a55hole\n"+
            "acrotomophilia\n"+
            "aeolus\n"+
            "ahole\n"+
            "alabama hot pocket\n"+
            "alaskan pipeline\n"+
            "anal\n"+
            "anal impaler\n"+
            "anal leakage\n"+
            "analprobe\n"+
            "anilingus\n"+
            "anus\n"+
            "apeshit\n"+
            "ar5e\n"+
            "areola\n"+
            "areole\n"+
            "arian\n"+
            "arrse\n"+
            "arse\n"+
            "arsehole\n"+
            "aryan\n"+
            "ass\n"+
            "ass fuck\n"+
            "ass fuck\n"+
            "ass hole\n"+
            "assbag\n"+
            "assbandit\n"+
            "assbang\n"+
            "assbanged\n"+
            "assbanger\n"+
            "assbangs\n"+
            "assbite\n"+
            "assclown\n"+
            "asscock\n"+
            "asscracker\n"+
            "asses\n"+
            "assface\n"+
            "assfaces\n"+
            "assfuck\n"+
            "assfucker\n"+
            "ass-fucker\n"+
            "assfukka\n"+
            "assgoblin\n"+
            "assh0le\n"+
            "asshat\n"+
            "ass-hat\n"+
            "asshead\n"+
            "assho1e\n"+
            "asshole\n"+
            "assholes\n"+
            "asshopper\n"+
            "ass-jabber\n"+
            "assjacker\n"+
            "asslick\n"+
            "asslicker\n"+
            "assmaster\n"+
            "assmonkey\n"+
            "assmucus\n"+
            "assmucus\n"+
            "assmunch\n"+
            "assmuncher\n"+
            "assnigger\n"+
            "asspirate\n"+
            "ass-pirate\n"+
            "assshit\n"+
            "assshole\n"+
            "asssucker\n"+
            "asswad\n"+
            "asswhole\n"+
            "asswipe\n"+
            "asswipes\n"+
            "auto erotic\n"+
            "autoerotic\n"+
            "axwound\n"+
            "azazel\n"+
            "azz\n"+
            "b!tch\n"+
            "b00bs\n"+
            "b17ch\n"+
            "b1tch\n"+
            "babeland\n"+
            "baby batter\n"+
            "baby juice\n"+
            "ball gag\n"+
            "ball gravy\n"+
            "ball kicking\n"+
            "ball licking\n"+
            "ball sack\n"+
            "ball sucking\n"+
            "ballbag\n"+
            "balls\n"+
            "ballsack\n"+
            "bampot\n"+
            "bang (one's) box\n"+
            "bangbros\n"+
            "bareback\n"+
            "barely legal\n"+
            "barenaked\n"+
            "barf\n"+
            "bastard\n"+
            "bastardo\n"+
            "bastards\n"+
            "bastinado\n"+
            "batty boy\n"+
            "bawdy\n"+
            "bbw\n"+
            "bdsm\n"+
            "beaner\n"+
            "beaners\n"+
            "beardedclam\n"+
            "beastial\n"+
            "beastiality\n"+
            "beatch\n"+
            "beaver\n"+
            "beaver cleaver\n"+
            "beaver lips\n"+
            "beef curtain\n"+
            "beef curtain\n"+
            "beef curtains\n"+
            "beeyotch\n"+
            "bellend\n"+
            "bender\n"+
            "beotch\n"+
            "bescumber\n"+
            "bestial\n"+
            "bestiality\n"+
            "bi+ch\n"+
            "biatch\n"+
            "big black\n"+
            "big breasts\n"+
            "big knockers\n"+
            "big tits\n"+
            "bigtits\n"+
            "bimbo\n"+
            "bimbos\n"+
            "bint\n"+
            "birdlock\n"+
            "bitch\n"+
            "bitch tit\n"+
            "bitch tit\n"+
            "bitchass\n"+
            "bitched\n"+
            "bitcher\n"+
            "bitchers\n"+
            "bitches\n"+
            "bitchin\n"+
            "bitching\n"+
            "bitchtits\n"+
            "bitchy\n"+
            "black cock\n"+
            "blonde action\n"+
            "blonde on blonde action\n"+
            "bloodclaat\n"+
            "bloody\n"+
            "bloody hell\n"+
            "blow job\n"+
            "blow me\n"+
            "blow mud\n"+
            "blow your load\n"+
            "blowjob\n"+
            "blowjobs\n"+
            "blue waffle\n"+
            "blue waffle\n"+
            "blumpkin\n"+
            "blumpkin\n"+
            "bod\n"+
            "bodily\n"+
            "boink\n"+
            "boiolas\n"+
            "bollock\n"+
            "bollocks\n"+
            "bollok\n"+
            "bollox\n"+
            "bondage\n"+
            "boned\n"+
            "boner\n"+
            "boners\n"+
            "bong\n"+
            "boob\n"+
            "boobies\n"+
            "boobs\n"+
            "booby\n"+
            "booger\n"+
            "bookie\n"+
            "boong\n"+
            "booobs\n"+
            "boooobs\n"+
            "booooobs\n"+
            "booooooobs\n"+
            "bootee\n"+
            "bootie\n"+
            "booty\n"+
            "booty call\n"+
            "booze\n"+
            "boozer\n"+
            "boozy\n"+
            "bosom\n"+
            "bosomy\n"+
            "breasts\n"+
            "Breeder\n"+
            "brotherfucker\n"+
            "brown showers\n"+
            "brunette action\n"+
            "buceta\n"+
            "bugger\n"+
            "bukkake\n"+
            "bull shit\n"+
            "bulldyke\n"+
            "bullet vibe\n"+
            "bullshit\n"+
            "bullshits\n"+
            "bullshitted\n"+
            "bullturds\n"+
            "bum\n"+
            "bum boy\n"+
            "bumblefuck\n"+
            "bumclat\n"+
            "bummer\n"+
            "buncombe\n"+
            "bung\n"+
            "bung hole\n"+
            "bunghole\n"+
            "bunny fucker\n"+
            "bust a load\n"+
            "bust a load\n"+
            "busty\n"+
            "butt\n"+
            "butt fuck\n"+
            "butt fuck\n"+
            "butt plug\n"+
            "buttcheeks\n"+
            "buttfuck\n"+
            "buttfucka\n"+
            "buttfucker\n"+
            "butthole\n"+
            "buttmuch\n"+
            "buttmunch\n"+
            "butt-pirate\n"+
            "buttplug\n"+
            "c.0.c.k\n"+
            "c.o.c.k.\n"+
            "c.u.n.t\n"+
            "c0ck\n"+
            "c-0-c-k\n"+
            "c0cksucker\n"+
            "caca\n"+
            "cacafuego\n"+
            "cahone\n"+
            "camel toe\n"+
            "cameltoe\n"+
            "camgirl\n"+
            "camslut\n"+
            "camwhore\n"+
            "carpet muncher\n"+
            "carpetmuncher\n"+
            "cawk\n"+
            "cervix\n"+
            "chesticle\n"+
            "chi-chi man\n"+
            "chick with a dick\n"+
            "child-fucker\n"+
            "chinc\n"+
            "chincs\n"+
            "chink\n"+
            "chinky\n"+
            "choad\n"+
            "choade\n"+
            "choade\n"+
            "choc ice\n"+
            "chocolate rosebuds\n"+
            "chode\n"+
            "chodes\n"+
            "chota bags\n"+
            "chota bags\n"+
            "cipa\n"+
            "circlejerk\n"+
            "cl1t\n"+
            "cleveland steamer\n"+
            "climax\n"+
            "clit\n"+
            "clit licker\n"+
            "clit licker\n"+
            "clitface\n"+
            "clitfuck\n"+
            "clitoris\n"+
            "clitorus\n"+
            "clits\n"+
            "clitty\n"+
            "clitty litter\n"+
            "clitty litter\n"+
            "clover clamps\n"+
            "clunge\n"+
            "clusterfuck\n"+
            "cnut\n"+
            "cocain\n"+
            "cocaine\n"+
            "coccydynia\n"+
            "cock\n"+
            "c-o-c-k\n"+
            "cock pocket\n"+
            "cock pocket\n"+
            "cock snot\n"+
            "cock snot\n"+
            "cock sucker\n"+
            "cockass\n"+
            "cockbite\n"+
            "cockblock\n"+
            "cockburger\n"+
            "cockeye\n"+
            "cockface\n"+
            "cockfucker\n"+
            "cockhead\n"+
            "cockholster\n"+
            "cockjockey\n"+
            "cockknocker\n"+
            "cockknoker\n"+
            "Cocklump\n"+
            "cockmaster\n"+
            "cockmongler\n"+
            "cockmongruel\n"+
            "cockmonkey\n"+
            "cockmunch\n"+
            "cockmuncher\n"+
            "cocknose\n"+
            "cocknugget\n"+
            "cocks\n"+
            "cockshit\n"+
            "cocksmith\n"+
            "cocksmoke\n"+
            "cocksmoker\n"+
            "cocksniffer\n"+
            "cocksuck\n"+
            "cocksuck\n"+
            "cocksucked\n"+
            "cocksucked\n"+
            "cocksucker\n"+
            "cock-sucker\n"+
            "cocksuckers\n"+
            "cocksucking\n"+
            "cocksucks\n"+
            "cocksucks\n"+
            "cocksuka\n"+
            "cocksukka\n"+
            "cockwaffle\n"+
            "coffin dodger\n"+
            "coital\n"+
            "cok\n"+
            "cokmuncher\n"+
            "coksucka\n"+
            "commie\n"+
            "condom\n"+
            "coochie\n"+
            "coochy\n"+
            "coon\n"+
            "coonnass\n"+
            "coons\n"+
            "cooter\n"+
            "cop some wood\n"+
            "cop some wood\n"+
            "coprolagnia\n"+
            "coprophilia\n"+
            "corksucker\n"+
            "cornhole\n"+
            "cornhole\n"+
            "corp whore\n"+
            "corp whore\n"+
            "corpulent\n"+
            "cox\n"+
            "crabs\n"+
            "crack\n"+
            "cracker\n"+
            "crackwhore\n"+
            "crap\n"+
            "crappy\n"+
            "creampie\n"+
            "cretin\n"+
            "crikey\n"+
            "cripple\n"+
            "crotte\n"+
            "cum\n"+
            "cum chugger\n"+
            "cum chugger\n"+
            "cum dumpster\n"+
            "cum dumpster\n"+
            "cum freak\n"+
            "cum freak\n"+
            "cum guzzler\n"+
            "cum guzzler\n"+
            "cumbubble\n"+
            "cumdump\n"+
            "cumdump\n"+
            "cumdumpster\n"+
            "cumguzzler\n"+
            "cumjockey\n"+
            "cummer\n"+
            "cummin\n"+
            "cumming\n"+
            "cums\n"+
            "cumshot\n"+
            "cumshots\n"+
            "cumslut\n"+
            "cumstain\n"+
            "cumtart\n"+
            "cunilingus\n"+
            "cunillingus\n"+
            "cunnie\n"+
            "cunnilingus\n"+
            "cunny\n"+
            "cunt\n"+
            "c-u-n-t\n"+
            "cunt hair\n"+
            "cunt hair\n"+
            "cuntass\n"+
            "cuntbag\n"+
            "cuntbag\n"+
            "cuntface\n"+
            "cunthole\n"+
            "cunthunter\n"+
            "cuntlick\n"+
            "cuntlick\n"+
            "cuntlicker\n"+
            "cuntlicker\n"+
            "cuntlicking\n"+
            "cuntlicking\n"+
            "cuntrag\n"+
            "cunts\n"+
            "cuntsicle\n"+
            "cuntsicle\n"+
            "cuntslut\n"+
            "cunt-struck\n"+
            "cunt-struck\n"+
            "cus\n"+
            "cut rope\n"+
            "cut rope\n"+
            "cyalis\n"+
            "cyberfuc\n"+
            "cyberfuck\n"+
            "cyberfuck\n"+
            "cyberfucked\n"+
            "cyberfucked\n"+
            "cyberfucker\n"+
            "cyberfuckers\n"+
            "cyberfucking\n"+
            "cyberfucking\n"+
            "d0ng\n"+
            "d0uch3\n"+
            "d0uche\n"+
            "d1ck\n"+
            "d1ld0\n"+
            "d1ldo\n"+
            "dago\n"+
            "dagos\n"+
            "dammit\n"+
            "damn\n"+
            "damned\n"+
            "damnit\n"+
            "darkie\n"+
            "darn\n"+
            "date rape\n"+
            "daterape\n"+
            "dawgie-style\n"+
            "deep throat\n"+
            "deepthroat\n"+
            "deggo\n"+
            "dendrophilia\n"+
            "dick\n"+
            "dick head\n"+
            "dick hole\n"+
            "dick hole\n"+
            "dick shy\n"+
            "dick shy\n"+
            "dickbag\n"+
            "dickbeaters\n"+
            "dickdipper\n"+
            "dickface\n"+
            "dickflipper\n"+
            "dickfuck\n"+
            "dickfucker\n"+
            "dickhead\n"+
            "dickheads\n"+
            "dickhole\n"+
            "dickish\n"+
            "dick-ish\n"+
            "dickjuice\n"+
            "dickmilk\n"+
            "dickmonger\n"+
            "dickripper\n"+
            "dicks\n"+
            "dicksipper\n"+
            "dickslap\n"+
            "dick-sneeze\n"+
            "dicksucker\n"+
            "dicksucking\n"+
            "dicktickler\n"+
            "dickwad\n"+
            "dickweasel\n"+
            "dickweed\n"+
            "dickwhipper\n"+
            "dickwod\n"+
            "dickzipper\n"+
            "diddle\n"+
            "dike\n"+
            "dildo\n"+
            "dildos\n"+
            "diligaf\n"+
            "dillweed\n"+
            "dimwit\n"+
            "dingle\n"+
            "dingleberries\n"+
            "dingleberry\n"+
            "dink\n"+
            "dinks\n"+
            "dipship\n"+
            "dipshit\n"+
            "dirsa\n"+
            "dirty\n"+
            "dirty pillows\n"+
            "dirty sanchez\n"+
            "dirty Sanchez\n"+
            "div\n"+
            "dlck\n"+
            "dog style\n"+
            "dog-fucker\n"+
            "doggie style\n"+
            "doggiestyle\n"+
            "doggie-style\n"+
            "doggin\n"+
            "dogging\n"+
            "doggy style\n"+
            "doggystyle\n"+
            "doggy-style\n"+
            "dolcett\n"+
            "domination\n"+
            "dominatrix\n"+
            "dommes\n"+
            "dong\n"+
            "donkey punch\n"+
            "donkeypunch\n"+
            "donkeyribber\n"+
            "doochbag\n"+
            "doofus\n"+
            "dookie\n"+
            "doosh\n"+
            "dopey\n"+
            "double dong\n"+
            "double penetration\n"+
            "Doublelift\n"+
            "douch3\n"+
            "douche\n"+
            "douchebag\n"+
            "douchebags\n"+
            "douche-fag\n"+
            "douchewaffle\n"+
            "douchey\n"+
            "dp action\n"+
            "drunk\n"+
            "dry hump\n"+
            "duche\n"+
            "dumass\n"+
            "dumb ass\n"+
            "dumbass\n"+
            "dumbasses\n"+
            "Dumbcunt\n"+
            "dumbfuck\n"+
            "dumbshit\n"+
            "dummy\n"+
            "dumshit\n"+
            "dvda\n"+
            "dyke\n"+
            "dykes\n"+
            "eat a dick\n"+
            "eat a dick\n"+
            "eat hair pie\n"+
            "eat hair pie\n"+
            "eat my ass\n"+
            "ecchi\n"+
            "ejaculate\n"+
            "ejaculated\n"+
            "ejaculates\n"+
            "ejaculates\n"+
            "ejaculating\n"+
            "ejaculating\n"+
            "ejaculatings\n"+
            "ejaculation\n"+
            "ejakulate\n"+
            "erect\n"+
            "erection\n"+
            "erotic\n"+
            "erotism\n"+
            "escort\n"+
            "essohbee\n"+
            "eunuch\n"+
            "extacy\n"+
            "extasy\n"+
            "f u c k\n"+
            "f u c k e r\n"+
            "f.u.c.k\n"+
            "f_u_c_k\n"+
            "f4nny\n"+
            "facial\n"+
            "fack\n"+
            "fag\n"+
            "fagbag\n"+
            "fagfucker\n"+
            "fagg\n"+
            "fagged\n"+
            "fagging\n"+
            "faggit\n"+
            "faggitt\n"+
            "faggot\n"+
            "faggotcock\n"+
            "faggots\n"+
            "faggs\n"+
            "fagot\n"+
            "fagots\n"+
            "fags\n"+
            "fagtard\n"+
            "faig\n"+
            "faigt\n"+
            "fanny\n"+
            "fannybandit\n"+
            "fannyflaps\n"+
            "fannyfucker\n"+
            "fanyy\n"+
            "fart\n"+
            "fartknocker\n"+
            "fatass\n"+
            "fcuk\n"+
            "fcuker\n"+
            "fcuking\n"+
            "fecal\n"+
            "feck\n"+
            "fecker\n"+
            "feist\n"+
            "felch\n"+
            "felcher\n"+
            "felching\n"+
            "fellate\n"+
            "fellatio\n"+
            "feltch\n"+
            "feltcher\n"+
            "female squirting\n"+
            "femdom\n"+
            "fenian\n"+
            "fice\n"+
            "figging\n"+
            "fingerbang\n"+
            "fingerfuck\n"+
            "fingerfuck\n"+
            "fingerfucked\n"+
            "fingerfucked\n"+
            "fingerfucker\n"+
            "fingerfucker\n"+
            "fingerfuckers\n"+
            "fingerfucking\n"+
            "fingerfucking\n"+
            "fingerfucks\n"+
            "fingerfucks\n"+
            "fingering\n"+
            "fist fuck\n"+
            "fist fuck\n"+
            "fisted\n"+
            "fistfuck\n"+
            "fistfucked\n"+
            "fistfucked\n"+
            "fistfucker\n"+
            "fistfucker\n"+
            "fistfuckers\n"+
            "fistfuckers\n"+
            "fistfucking\n"+
            "fistfucking\n"+
            "fistfuckings\n"+
            "fistfuckings\n"+
            "fistfucks\n"+
            "fistfucks\n"+
            "fisting\n"+
            "fisty\n"+
            "flamer\n"+
            "flange\n"+
            "flaps\n"+
            "fleshflute\n"+
            "flog the log\n"+
            "flog the log\n"+
            "floozy\n"+
            "foad\n"+
            "foah\n"+
            "fondle\n"+
            "foobar\n"+
            "fook\n"+
            "fooker\n"+
            "foot fetish\n"+
            "footjob\n"+
            "foreskin\n"+
            "freex\n"+
            "frenchify\n"+
            "frigg\n"+
            "frigga\n"+
            "frotting\n"+
            "fubar\n"+
            "fuc\n"+
            "fuck\n"+
            "fuck\n"+
            "f-u-c-k\n"+
            "fuck buttons\n"+
            "fuck hole\n"+
            "fuck hole\n"+
            "Fuck off\n"+
            "fuck puppet\n"+
            "fuck puppet\n"+
            "fuck trophy\n"+
            "fuck trophy\n"+
            "fuck yo mama\n"+
            "fuck yo mama\n"+
            "fuck you\n"+
            "fucka\n"+
            "fuckass\n"+
            "fuck-ass\n"+
            "fuck-ass\n"+
            "fuckbag\n"+
            "fuck-bitch\n"+
            "fuck-bitch\n"+
            "fuckboy\n"+
            "fuckbrain\n"+
            "fuckbutt\n"+
            "fuckbutter\n"+
            "fucked\n"+
            "fuckedup\n"+
            "fucker\n"+
            "fuckers\n"+
            "fuckersucker\n"+
            "fuckface\n"+
            "fuckhead\n"+
            "fuckheads\n"+
            "fuckhole\n"+
            "fuckin\n"+
            "fucking\n"+
            "fuckings\n"+
            "fuckingshitmotherfucker\n"+
            "fuckme\n"+
            "fuckme\n"+
            "fuckmeat\n"+
            "fuckmeat\n"+
            "fucknugget\n"+
            "fucknut\n"+
            "fucknutt\n"+
            "fuckoff\n"+
            "fucks\n"+
            "fuckstick\n"+
            "fucktard\n"+
            "fuck-tard\n"+
            "fucktards\n"+
            "fucktart\n"+
            "fucktoy\n"+
            "fucktoy\n"+
            "fucktwat\n"+
            "fucku\n"+
            "fuckup\n"+
            "fuckwad\n"+
            "fuckwhit\n"+
            "fuckwit\n"+
            "fuckwitt\n"+
            "fuckyou\n"+
            "fudge packer\n"+
            "fudgepacker\n"+
            "fudge-packer\n"+
            "fuk\n"+
            "fuker\n"+
            "fukker\n"+
            "fukkers\n"+
            "fukkin\n"+
            "fuks\n"+
            "fuk u\n"+
            "fukwhit\n"+
            "fukwit\n"+
            "fuq\n"+
            "futanari\n"+
            "fux\n"+
            "fux0r\n"+
            "fvck\n"+
            "fxck\n"+
            "gae\n"+
            "gai\n"+
            "gang bang\n"+
            "gangbang\n"+
            "gang-bang\n"+
            "gang-bang\n"+
            "gangbanged\n"+
            "gangbangs\n"+
            "ganja\n"+
            "gash\n"+
            "gassy ass\n"+
            "gassy ass\n"+
            "gay\n"+
            "gay sex\n"+
            "gayass\n"+
            "gaybob\n"+
            "gaydo\n"+
            "gayfuck\n"+
            "gayfuckist\n"+
            "gaylord\n"+
            "gays\n"+
            "gaysex\n"+
            "gaytard\n"+
            "gaywad\n"+
            "gender bender\n"+
            "genitals\n"+
            "gey\n"+
            "gfy\n"+
            "ghay\n"+
            "ghey\n"+
            "giant cock\n"+
            "gigolo\n"+
            "ginger\n"+
            "gippo\n"+
            "girl on\n"+
            "girl on top\n"+
            "girls gone wild\n"+
            "git\n"+
            "glans\n"+
            "goatcx\n"+
            "goatse\n"+
            "god\n"+
            "god damn\n"+
            "godamn\n"+
            "godamnit\n"+
            "goddam\n"+
            "god-dam\n"+
            "goddammit\n"+
            "goddamn\n"+
            "goddamned\n"+
            "god-damned\n"+
            "goddamnit\n"+
            "godsdamn\n"+
            "gokkun\n"+
            "golden shower\n"+
            "goldenshower\n"+
            "golliwog\n"+
            "gonad\n"+
            "gonads\n"+
            "goo girl\n"+
            "gooch\n"+
            "goodpoop\n"+
            "gook\n"+
            "gooks\n"+
            "goregasm\n"+
            "gringo\n"+
            "grope\n"+
            "group sex\n"+
            "gspot\n"+
            "g-spot\n"+
            "gtfo\n"+
            "guido\n"+
            "guro\n"+
            "h0m0\n"+
            "h0mo\n"+
            "ham flap\n"+
            "ham flap\n"+
            "hand job\n"+
            "handjob\n"+
            "hard core\n"+
            "hard on\n"+
            "hardcore\n"+
            "hardcoresex\n"+
            "he11\n"+
            "hebe\n"+
            "heeb\n"+
            "hell\n"+
            "hemp\n"+
            "hentai\n"+
            "heroin\n"+
            "herp\n"+
            "herpes\n"+
            "herpy\n"+
            "heshe\n"+
            "he-she\n"+
            "hircismus\n"+
            "hitler\n"+
            "hiv\n"+
            "ho\n"+
            "hoar\n"+
            "hoare\n"+
            "hobag\n"+
            "hoe\n"+
            "hoer\n"+
            "holy shit\n"+
            "hom0\n"+
            "homey\n"+
            "homo\n"+
            "homodumbshit\n"+
            "homoerotic\n"+
            "homoey\n"+
            "honkey\n"+
            "honky\n"+
            "hooch\n"+
            "hookah\n"+
            "hooker\n"+
            "hoor\n"+
            "hootch\n"+
            "hooter\n"+
            "hooters\n"+
            "hore\n"+
            "horniest\n"+
            "horny\n"+
            "hot carl\n"+
            "hot chick\n"+
            "hotsex\n"+
            "how to kill\n"+
            "how to murdep\n"+
            "how to murder\n"+
            "huge fat\n"+
            "hump\n"+
            "humped\n"+
            "humping\n"+
            "hun\n"+
            "hussy\n"+
            "hymen\n"+
            "iap\n"+
            "iberian slap\n"+
            "inbred\n"+
            "incest\n"+
            "injun\n"+
            "intercourse\n"+
            "jack off\n"+
            "jackass\n"+
            "jackasses\n"+
            "jackhole\n"+
            "jackoff\n"+
            "jack-off\n"+
            "jaggi\n"+
            "jagoff\n"+
            "jail bait\n"+
            "jailbait\n"+
            "jap\n"+
            "japs\n"+
            "jelly donut\n"+
            "jerk\n"+
            "jerk off\n"+
            "jerk0ff\n"+
            "jerkass\n"+
            "jerked\n"+
            "jerkoff\n"+
            "jerk-off\n"+
            "jigaboo\n"+
            "jiggaboo\n"+
            "jiggerboo\n"+
            "jism\n"+
            "jiz\n"+
            "jiz\n"+
            "jizm\n"+
            "jizm\n"+
            "jizz\n"+
            "jizzed\n"+
            "jock\n"+
            "juggs\n"+
            "jungle bunny\n"+
            "junglebunny\n"+
            "junkie\n"+
            "junky\n"+
            "kafir\n"+
            "kawk\n"+
            "kike\n"+
            "kikes\n"+
            "kill\n"+
            "kinbaku\n"+
            "kinkster\n"+
            "kinky\n"+
            "klan\n"+
            "knob\n"+
            "knob end\n"+
            "knobbing\n"+
            "knobead\n"+
            "knobed\n"+
            "knobend\n"+
            "knobhead\n"+
            "knobjocky\n"+
            "knobjokey\n"+
            "kock\n"+
            "kondum\n"+
            "kondums\n"+
            "kooch\n"+
            "kooches\n"+
            "kootch\n"+
            "kraut\n"+
            "kum\n"+
            "kummer\n"+
            "kumming\n"+
            "kums\n"+
            "kunilingus\n"+
            "kunja\n"+
            "kunt\n"+
            "kwif\n"+
            "kwif\n"+
            "kyke\n"+
            "l3i+ch\n"+
            "l3itch\n"+
            "labia\n"+
            "lameass\n"+
            "lardass\n"+
            "leather restraint\n"+
            "leather straight jacket\n"+
            "lech\n"+
            "lemon party\n"+
            "LEN\n"+
            "leper\n"+
            "lesbian\n"+
            "lesbians\n"+
            "lesbo\n"+
            "lesbos\n"+
            "lez\n"+
            "lezza/lesbo\n"+
            "lezzie\n"+
            "lmao\n"+
            "lmfao\n"+
            "loin\n"+
            "loins\n"+
            "lolita\n"+
            "looney\n"+
            "lovemaking\n"+
            "lube\n"+
            "lust\n"+
            "lusting\n"+
            "lusty\n"+
            "m0f0\n"+
            "m0fo\n"+
            "m45terbate\n"+
            "ma5terb8\n"+
            "ma5terbate\n"+
            "mafugly\n"+
            "mafugly\n"+
            "make me come\n"+
            "male squirting\n"+
            "mams\n"+
            "masochist\n"+
            "massa\n"+
            "masterb8\n"+
            "masterbat*\n"+
            "masterbat3\n"+
            "masterbate\n"+
            "master-bate\n"+
            "master-bate\n"+
            "masterbating\n"+
            "masterbation\n"+
            "masterbations\n"+
            "masturbate\n"+
            "masturbating\n"+
            "masturbation\n"+
            "maxi\n"+
            "mcfagget\n"+
            "menage a trois\n"+
            "menses\n"+
            "menstruate\n"+
            "menstruation\n"+
            "meth\n"+
            "m-fucking\n"+
            "mick\n"+
            "microphallus\n"+
            "middle finger\n"+
            "midget\n"+
            "milf\n"+
            "minge\n"+
            "minger\n"+
            "missionary position\n"+
            "mof0\n"+
            "mofo\n"+
            "mo-fo\n"+
            "molest\n"+
            "mong\n"+
            "moo moo foo foo\n"+
            "moolie\n"+
            "moron\n"+
            "mothafuck\n"+
            "mothafucka\n"+
            "mothafuckas\n"+
            "mothafuckaz\n"+
            "mothafucked\n"+
            "mothafucked\n"+
            "mothafucker\n"+
            "mothafuckers\n"+
            "mothafuckin\n"+
            "mothafucking\n"+
            "mothafucking\n"+
            "mothafuckings\n"+
            "mothafucks\n"+
            "mother fucker\n"+
            "mother fucker\n"+
            "motherfuck\n"+
            "motherfucka\n"+
            "motherfucked\n"+
            "motherfucker\n"+
            "motherfuckers\n"+
            "motherfuckin\n"+
            "motherfucking\n"+
            "motherfuckings\n"+
            "motherfuckka\n"+
            "motherfucks\n"+
            "mound of venus\n"+
            "mr hands\n"+
            "muff\n"+
            "muff diver\n"+
            "muff puff\n"+
            "muff puff\n"+
            "muffdiver\n"+
            "muffdiving\n"+
            "munging\n"+
            "munter\n"+
            "murder\n"+
            "mutha\n"+
            "muthafecker\n"+
            "muthafuckker\n"+
            "muther\n"+
            "mutherfucker\n"+
            "n1gga\n"+
            "n1gger\n"+
            "naked\n"+
            "nambla\n"+
            "napalm\n"+
            "nappy\n"+
            "nawashi\n"+
            "nazi\n"+
            "nazism\n"+
            "need the dick\n"+
            "need the dick\n"+
            "negro\n"+
            "neonazi\n"+
            "nig nog\n"+
            "nigaboo\n"+
            "nigg3r\n"+
            "nigg4h\n"+
            "nigga\n"+
            "niggah\n"+
            "niggas\n"+
            "niggaz\n"+
            "nigger\n"+
            "niggers\n"+
            "niggle\n"+
            "niglet\n"+
            "nig-nog\n"+
            "nimphomania\n"+
            "nimrod\n"+
            "ninny\n"+
            "ninnyhammer\n"+
            "nipple\n"+
            "nipples\n"+
            "nob\n"+
            "nob jokey\n"+
            "nobhead\n"+
            "nobjocky\n"+
            "nobjokey\n"+
            "nonce\n"+
            "nsfw images\n"+
            "nude\n"+
            "nudity\n"+
            "numbnuts\n"+
            "nut butter\n"+
            "nut butter\n"+
            "nut sack\n"+
            "nutsack\n"+
            "nutter\n"+
            "nympho\n"+
            "nymphomania\n"+
            "octopussy\n"+
            "old bag\n"+
            "omg\n"+
            "omorashi\n"+
            "one cup two girls\n"+
            "one guy one jar\n"+
            "opiate\n"+
            "opium\n"+
            "orally\n"+
            "organ\n"+
            "orgasim\n"+
            "orgasims\n"+
            "orgasm\n"+
            "orgasmic\n"+
            "orgasms\n"+
            "orgies\n"+
            "orgy\n"+
            "ovary\n"+
            "ovum\n"+
            "ovums\n"+
            "p.u.s.s.y.\n"+
            "p0rn\n"+
            "paedophile\n"+
            "paki\n"+
            "panooch\n"+
            "pansy\n"+
            "pantie\n"+
            "panties\n"+
            "panty\n"+
            "pawn\n"+
            "pcp\n"+
            "pecker\n"+
            "peckerhead\n"+
            "pedo\n"+
            "pedobear\n"+
            "pedophile\n"+
            "pedophilia\n"+
            "pedophiliac\n"+
            "pee\n"+
            "peepee\n"+
            "pegging\n"+
            "penetrate\n"+
            "penetration\n"+
            "penial\n"+
            "penile\n"+
            "penis\n"+
            "penisbanger\n"+
            "penisfucker\n"+
            "penispuffer\n"+
            "perversion\n"+
            "phallic\n"+
            "phone sex\n"+
            "phonesex\n"+
            "phuck\n"+
            "phuk\n"+
            "phuked\n"+
            "phuking\n"+
            "phukked\n"+
            "phukking\n"+
            "phuks\n"+
            "phuq\n"+
            "piece of shit\n"+
            "pigfucker\n"+
            "pikey\n"+
            "pillowbiter\n"+
            "pimp\n"+
            "pimpis\n"+
            "pinko\n"+
            "piss\n"+
            "piss off\n"+
            "piss pig\n"+
            "pissed\n"+
            "pissed off\n"+
            "pisser\n"+
            "pissers\n"+
            "pisses\n"+
            "pisses\n"+
            "pissflaps\n"+
            "pissin\n"+
            "pissin\n"+
            "pissing\n"+
            "pissoff\n"+
            "pissoff\n"+
            "piss-off\n"+
            "pisspig\n"+
            "playboy\n"+
            "pleasure chest\n"+
            "pms\n"+
            "polack\n"+
            "pole smoker\n"+
            "polesmoker\n"+
            "pollock\n"+
            "ponyplay\n"+
            "poof\n"+
            "poon\n"+
            "poonani\n"+
            "poonany\n"+
            "poontang\n"+
            "poop\n"+
            "poop chute\n"+
            "poopchute\n"+
            "Poopuncher\n"+
            "porch monkey\n"+
            "porchmonkey\n"+
            "porn\n"+
            "porno\n"+
            "pornography\n"+
            "pornos\n"+
            "pot\n"+
            "potty\n"+
            "prick\n"+
            "pricks\n"+
            "prickteaser\n"+
            "prig\n"+
            "prince albert piercing\n"+
            "prod\n"+
            "pron\n"+
            "prostitute\n"+
            "prude\n"+
            "psycho\n"+
            "pthc\n"+
            "pube\n"+
            "pubes\n"+
            "pubic\n"+
            "pubis\n"+
            "punani\n"+
            "punanny\n"+
            "punany\n"+
            "punkass\n"+
            "punky\n"+
            "punta\n"+
            "puss\n"+
            "pusse\n"+
            "pussi\n"+
            "pussies\n"+
            "pussy\n"+
            "pussy fart\n"+
            "pussy fart\n"+
            "pussy palace\n"+
            "pussy palace\n"+
            "pussylicking\n"+
            "pussypounder\n"+
            "pussys\n"+
            "pust\n"+
            "puto\n"+
            "queaf\n"+
            "queaf\n"+
            "queef\n"+
            "queer\n"+
            "queerbait\n"+
            "queerhole\n"+
            "queero\n"+
            "queers\n"+
            "quicky\n"+
            "quim\n"+
            "racy\n"+
            "raghead\n"+
            "raging boner\n"+
            "rape\n"+
            "raped\n"+
            "raper\n"+
            "rapey\n"+
            "raping\n"+
            "rapist\n"+
            "raunch\n"+
            "rectal\n"+
            "rectum\n"+
            "rectus\n"+
            "reefer\n"+
            "reetard\n"+
            "reich\n"+
            "renob\n"+
            "retard\n"+
            "retarded\n"+
            "reverse cowgirl\n"+
            "revue\n"+
            "rimjaw\n"+
            "rimjob\n"+
            "rimming\n"+
            "ritard\n"+
            "rosy palm\n"+
            "rosy palm and her 5 sisters\n"+
            "rtard\n"+
            "r-tard\n"+
            "rubbish\n"+
            "rum\n"+
            "rump\n"+
            "rumprammer\n"+
            "ruski\n"+
            "rusty trombone\n"+
            "s hit\n"+
            "s&m\n"+
            "s.h.i.t.\n"+
            "s.o.b.\n"+
            "s_h_i_t\n"+
            "s0b\n"+
            "sadism\n"+
            "sadist\n"+
            "sambo\n"+
            "sand nigger\n"+
            "sandbar\n"+
            "sandbar\n"+
            "Sandler\n"+
            "sandnigger\n"+
            "sanger\n"+
            "santorum\n"+
            "sausage queen\n"+
            "sausage queen\n"+
            "scag\n"+
            "scantily\n"+
            "scat\n"+
            "schizo\n"+
            "schlong\n"+
            "scissoring\n"+
            "screw\n"+
            "screwed\n"+
            "screwing\n"+
            "scroat\n"+
            "scrog\n"+
            "scrot\n"+
            "scrote\n"+
            "scrotum\n"+
            "scrud\n"+
            "scum\n"+
            "seaman\n"+
            "seamen\n"+
            "seduce\n"+
            "seks\n"+
            "semen\n"+
            "sex\n"+
            "sexo\n"+
            "sexual\n"+
            "sexy\n"+
            "sh!+\n"+
            "sh!t\n"+
            "sh1t\n"+
            "s-h-1-t\n"+
            "shag\n"+
            "shagger\n"+
            "shaggin\n"+
            "shagging\n"+
            "shamedame\n"+
            "shaved beaver\n"+
            "shaved pussy\n"+
            "shemale\n"+
            "shi+\n"+
            "shibari\n"+
            "shirt lifter\n"+
            "shit\n"+
            "s-h-i-t\n"+
            "shit ass\n"+
            "shit fucker\n"+
            "shit fucker\n"+
            "shitass\n"+
            "shitbag\n"+
            "shitbagger\n"+
            "shitblimp\n"+
            "shitbrains\n"+
            "shitbreath\n"+
            "shitcanned\n"+
            "shitcunt\n"+
            "shitdick\n"+
            "shite\n"+
            "shiteater\n"+
            "shited\n"+
            "shitey\n"+
            "shitface\n"+
            "shitfaced\n"+
            "shitfuck\n"+
            "shitfull\n"+
            "shithead\n"+
            "shitheads\n"+
            "shithole\n"+
            "shithouse\n"+
            "shiting\n"+
            "shitings\n"+
            "shits\n"+
            "shitspitter\n"+
            "shitstain\n"+
            "shitt\n"+
            "shitted\n"+
            "shitter\n"+
            "shitters\n"+
            "shitters\n"+
            "shittier\n"+
            "shittiest\n"+
            "shitting\n"+
            "shittings\n"+
            "shitty\n"+
            "shiz\n"+
            "shiznit\n"+
            "shota\n"+
            "shrimping\n"+
            "sissy\n"+
            "skag\n"+
            "skank\n"+
            "skeet\n"+
            "skullfuck\n"+
            "slag\n"+
            "slanteye\n"+
            "slave\n"+
            "sleaze\n"+
            "sleazy\n"+
            "slope\n"+
            "slope\n"+
            "slut\n"+
            "slut bucket\n"+
            "slut bucket\n"+
            "slutbag\n"+
            "slutdumper\n"+
            "slutkiss\n"+
            "sluts\n"+
            "smartass\n"+
            "smartasses\n"+
            "smeg\n"+
            "smegma\n"+
            "smut\n"+
            "smutty\n"+
            "snatch\n"+
            "sniper\n"+
            "snowballing\n"+
            "snuff\n"+
            "s-o-b\n"+
            "sod off\n"+
            "sodom\n"+
            "sodomize\n"+
            "sodomy\n"+
            "son of a bitch\n"+
            "son of a motherless goat\n"+
            "son of a whore\n"+
            "son-of-a-bitch\n"+
            "souse\n"+
            "soused\n"+
            "spac\n"+
            "spade\n"+
            "sperm\n"+
            "spic\n"+
            "spick\n"+
            "spik\n"+
            "spiks\n"+
            "splooge\n"+
            "splooge moose\n"+
            "spooge\n"+
            "spook\n"+
            "spread legs\n"+
            "spunk\n"+
            "stfu\n"+
            "stiffy\n"+
            "stoned\n"+
            "strap on\n"+
            "strapon\n"+
            "strappado\n"+
            "strip\n"+
            "strip club\n"+
            "stroke\n"+
            "stupid\n"+
            "style doggy\n"+
            "suck\n"+
            "suckass\n"+
            "sucked\n"+
            "sucking\n"+
            "sucks\n"+
            "suicide girls\n"+
            "sultry women\n"+
            "sumofabiatch\n"+
            "swastika\n"+
            "swinger\n"+
            "t1t\n"+
            "t1tt1e5\n"+
            "t1tties\n"+
            "taff\n"+
            "taig\n"+
            "tainted love\n"+
            "taking the piss\n"+
            "tampon\n"+
            "tard\n"+
            "tart\n"+
            "taste my\n"+
            "tawdry\n"+
            "tea bagging\n"+
            "teabagging\n"+
            "teat\n"+
            "teets\n"+
            "teez\n"+
            "teste\n"+
            "testee\n"+
            "testes\n"+
            "testical\n"+
            "testicle\n"+
            "testis\n"+
            "threesome\n"+
            "throating\n"+
            "thrust\n"+
            "thug\n"+
            "thundercunt\n"+
            "tied up\n"+
            "tight white\n"+
            "tinkle\n"+
            "tit\n"+
            "tit wank\n"+
            "tit wank\n"+
            "titfuck\n"+
            "titi\n"+
            "tities\n"+
            "tits\n"+
            "titt\n"+
            "tittie5\n"+
            "tittiefucker\n"+
            "titties\n"+
            "titty\n"+
            "tittyfuck\n"+
            "tittyfucker\n"+
            "tittywank\n"+
            "titwank\n"+
            "toke\n"+
            "tongue in a\n"+
            "toots\n"+
            "topless\n"+
            "tosser\n"+
            "towelhead\n"+
            "tramp\n"+
            "tranny\n"+
            "transsexual\n"+
            "trashy\n"+
            "tribadism\n"+
            "trumped\n"+
            "tub girl\n"+
            "tubgirl\n"+
            "turd\n"+
            "tush\n"+
            "tushy\n"+
            "tw4t\n"+
            "twat\n"+
            "twathead\n"+
            "twatlips\n"+
            "twats\n"+
            "twatty\n"+
            "twatwaffle\n"+
            "twink\n"+
            "twinkie\n"+
            "two fingers\n"+
            "two fingers with tongue\n"+
            "two girls one cup\n"+
            "twunt\n"+
            "twunter\n"+
            "ugly\n"+
            "unclefucker\n"+
            "undies\n"+
            "undressing\n"+
            "unwed\n"+
            "upskirt\n"+
            "urethra play\n"+
            "urinal\n"+
            "urine\n"+
            "urophilia\n"+
            "uterus\n"+
            "uzi\n"+
            "v14gra\n"+
            "v1gra\n"+
            "vag\n"+
            "vagina\n"+
            "vajayjay\n"+
            "va-j-j\n"+
            "valium\n"+
            "venus mound\n"+
            "veqtable\n"+
            "viagra\n"+
            "vibrator\n"+
            "violet wand\n"+
            "virgin\n"+
            "vixen\n"+
            "vjayjay\n"+
            "vodka\n"+
            "vomit\n"+
            "vorarephilia\n"+
            "voyeur\n"+
            "vulgar\n"+
            "vulva\n"+
            "w00se\n"+
            "wad\n"+
            "wang\n"+
            "wank\n"+
            "wanker\n"+
            "wankjob\n"+
            "wanky\n"+
            "wazoo\n"+
            "wedgie\n"+
            "weed\n"+
            "weenie\n"+
            "weewee\n"+
            "weiner\n"+
            "weirdo\n"+
            "wench\n"+
            "wet dream\n"+
            "wetback\n"+
            "wh0re\n"+
            "wh0reface\n"+
            "white power\n"+
            "whiz\n"+
            "whoar\n"+
            "whoralicious\n"+
            "whore\n"+
            "whorealicious\n"+
            "whorebag\n"+
            "whored\n"+
            "whoreface\n"+
            "whorehopper\n"+
            "whorehouse\n"+
            "whores\n"+
            "whoring\n"+
            "wigger\n"+
            "willies\n"+
            "willy\n"+
            "window licker\n"+
            "wiseass\n"+
            "wiseasses\n"+
            "wog\n"+
            "womb\n"+
            "wop\n"+
            "wrapping men\n"+
            "wrinkled starfish\n"+
            "wtf\n"+
            "xrated\n"+
            "x-rated\n"+
            "xx\n"+
            "xxx\n"+
            "yaoi\n"+
            "yeasty\n"+
            "yellow showers\n"+
            "yid\n"+
            "yiffy\n"+
            "yobbo\n"+
            "zibbi\n"+
            "zoophilia\n"+
            "zubb\n";
}