package org.grobid.core.engines;

import org.grobid.core.EngineMockTest;
import org.grobid.core.document.DocumentPiece;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.grobid.service.DictionaryPaths.PATH_DICTIONARY_BODY_SEGMENTATATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by med on 07.12.16.
 */
public class DictionaryBodySegmentationTest extends EngineMockTest{
    DictionaryBodySegmentationParser target;

    @Before
    public void setUp() throws Exception {
        target = new DictionaryBodySegmentationParser();
    }

    @Test
    public void testProcess() throws Exception {
//        File input = new File(this.getClass().getResource("close.pdf").toURI());

        String body = "<lb/>Antonyms <lb/>1.3.3 <lb/>Derived terms <lb/>1.3.4 <lb/>Translations <lb/>1.4 <lb/>Noun <lb/>1.4.1 <lb/>Synonyms <lb/>1.4.2 <lb/>Derived terms <lb/>1.4.3 <lb/>Coordinate terms <lb/>1.4.4 <lb/>Translations <lb/>1.5 <lb/>Adverb <lb/>1.6 <lb/>References <lb/>1.7 <lb/>See also <lb/>1.8 <lb/>Anagrams <lb/>From Middle English cold, from Old English, specifically Anglian cald. The West Saxon form, ċeald (\"cold\" ), survived as early <lb/>Middle English cheald, cheld or chald. [1] Both descended from Proto-Germanic *kaldaz, a participle form of *kalaną (\"to be cold\" ), <lb/>from Proto-Indo-European *gel-(\"cold\" ). Cognate with Scots cald, cauld (\"cold\" ), Saterland Frisian koold (\"cold\" ), West Frisian <lb/>kâld (\"cold\" ), Dutch koud (\"cold\" ), Low German kold, koolt, koold (\"cold\" ), German kalt (\"cold\" ), Danish kold (\"cold\" ), Norwegian <lb/>kald (\"cold\" ), Swedish kall (\"cold\" ). <lb/>(UK) IPA (key) : /kəʊld/, /kɔʊld/ <lb/>(US) enPR: kōld, IPA (key) : /koʊld/ <lb/>Audio (UK) <lb/>Audio (US) <lb/>Homophone: coaled <lb/>Rhymes: -əʊld <lb/>Contents <lb/>English <lb/>Etymology <lb/>Pronunciation <lb/>0:00 <lb/>0:00 <lb/>Adjective <lb/>cold (comparative colder, superlative coldest) <lb/>1. (of a thing) Having a low temperature. <lb/>A cold wind whistled through the trees. <lb/>2. (of the weather) Causing the air to be cold. <lb/>The forecast is that it will be very cold today. <lb/>3. (of a person or animal ) Feeling the sensation of coldness, especially to the point of discomfort. <lb/>She was so cold she was shivering. <lb/>4. Unfriendly, emotionally distant or unfeeling. <lb/>She shot me a cold glance before turning her back. <lb/>5. Dispassionate, not prejudiced or partisan, impartial. <lb/>Let's look at this tomorrow with a cold head. <lb/>He's a nice guy, but the cold facts say we should fire him. <lb/>The cold truth is that states rarely undertake military action unless their national interests are at stake. <lb/>6. Completely unprepared; without introduction. <lb/>He was assigned cold calls for the first three months. <lb/>7. Unconscious or deeply asleep; deprived of the metaphorical heat associated with life or consciousness. <lb/>I knocked him out cold. <lb/>After one more beer he passed out cold. <lb/>8. (usually with \"have\" or \"know\" transitively ) Perfectly, exactly, completely; by heart. <lb/>Practice your music scales until you know them cold. <lb/>Try both these maneuvers until you have them cold and can do them in the dark without thinking. <lb/>Rehearse your lines until you have them down cold. <lb/>Keep that list in front of you, or memorize it cold. <lb/>9. (usually with \"have\" transitively ) Cornered, done for . <lb/>With that receipt, we have them cold for fraud. <lb/>Criminal interrogation. Initially they will dream up explanations faster than you could ever do so, but when <lb/>they become fatigued, often they will acknowledge that you have them cold. <lb/>10. (obsolete) Not pungent or acrid. <lb/>11. (obsolete) Unexciting; dull; uninteresting. <lb/>12. Affecting the sense of smell (as of hunting do gs) only feebly; having lost its odour . <lb/>a cold scent <lb/>13. (obsolete) Not sensitive; not acute. <lb/>14. Distant; said, in the game of hunting for some object, of a seeker remote from the thing concealed. Compare warm <lb/>and hot. <lb/>You're cold … getting warmer … hot! Y ou've found it! <lb/>15. (painting) Having a bluish effect; not warm in colour . <lb/>(of a thing, having a low temperature ): chilled, chilly, freezing, frigid, glacial, icy, cool <lb/>(of the weather): (UK, slang) brass monkeys, nippy, parky, taters <lb/>(of a person or animal ): <lb/>Synonyms <lb/>(unfriendly): aloof, distant, hostile, standoffish, unfriendly, unwelcoming <lb/>(unprepared): unprepared, unready <lb/>See also Wikisaurus:cold <lb/>(having a low temperature): baking, boiling, heated, hot, scorching, searing, torrid, warm <lb/>(of the weather): hot (See the corresponding synonyms of hot.) <lb/>(of a person or animal ): hot (See the corresponding synonyms of hot.) <lb/>(unfriendly): amiable, friendly, welcoming <lb/>(unprepared): prepared, primed, ready <lb/>Terms derived from the adjective cold <lb/>having a low temperature <lb/>of the weather <lb/>of a person <lb/>unfriendly <lb/>unprepared <lb/>The translations below need to be checked and inserted above into the appropriate <lb/>translation tables, removing any numbers. Numbers do not necessarily match those in <lb/>definitions. See instructions at Wiktionary:Entry layout#Translations. <lb/>Translations to be checked <lb/>cold (plural colds) <lb/>1. A condition of low temperature. <lb/>Come in, out of the cold. <lb/>2. (medicine) A common, usually harmless, viral illness, usually with congestion of the nasal passages and sometimes <lb/>fever. <lb/>I caught a miserable cold and had to stay home for a week. <lb/>(low temperature): coldness <lb/>(illness): common cold, coryza, head cold <lb/>Terms derived from the noun cold <lb/>Antonyms <lb/>Derived terms <lb/>Translations <lb/>Noun <lb/>Synonyms <lb/>Derived terms <lb/>freeze, frost <lb/>low temperature <lb/>illness <lb/>cold (comparative more cold, superlative most cold) <lb/>1. While at low temperature. <lb/>The steel was processed cold. <lb/>2. Without preparation. <lb/>The speaker went in cold and floundered for a topic. <lb/>3. With finality. <lb/>I knocked him out cold. <lb/>4. (slang, informal, dated) In a cold, frank, or realistically honest manner. <lb/>Now Little Bo Peep cold lost her sheep / And Rip van W inkle fell the hell asleep -Run Dmc, Peter Piper. <lb/>1. ^ \"cold (http://www.oed.com/view/Entry/36101) \", in OED Online, Oxford: Oxford University Press, launched 2000. <lb/>cool <lb/>fresh <lb/>lukewarm <lb/>tepid <lb/>clod <lb/>Retrieved from \"https://en.wiktionary.org/w/index.php?title=cold&oldid=47824787\" <lb/>"
        ;
//        System.out.print("last" + body.lastIndexOf("&"));
        System.out.println("nextToand " + body.charAt(5824));
        body = body.replace("&","&amp");
        System.out.println("nextToand " + body.charAt(5824));
        assertThat(body, notNullValue());

    }

}
