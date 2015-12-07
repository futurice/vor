package com.futurice.hereandnow;

import android.test.suitebuilder.annotation.*;

import com.futurice.hereandnow.utils.HereAndNowUtils;

import junit.framework.*;

import static org.assertj.core.api.Assertions.*;

@SmallTest
public class HereAndNowUtilsTest extends TestCase {

    public void testCleanupTagLeadingSpaces() throws Exception {
        assertThat(HereAndNowUtils.cleanupTag("   @this", '@')).isEqualTo("@this");
    }

    public void testCleanupTagTrainlingLineBreakAndSpaces() throws Exception {
        assertThat(HereAndNowUtils.cleanupTag("@this  \n  ", '@')).isEqualTo("@this");
    }

    public void testCleanupTagNoMark() throws Exception {
        assertThat(HereAndNowUtils.cleanupTag("  untagged..", '#')).isEqualTo("#untagged");
    }

    public void testCleanupTagExtraMark() throws Exception {
        assertThat(HereAndNowUtils.cleanupTag("  # # ##untagged..", '#')).isEqualTo("#untagged");
    }

    public void testCleanupTagUnsupportedInMiddle() throws Exception {
        assertThat(HereAndNowUtils.cleanupTag("un..55 tagged..", '#')).isEqualTo("#un55tagged");
    }

    public void testLeadingNumbers() throws Exception {
        assertThat(HereAndNowUtils.cleanupTag("66tagg 8 ed7..", '#')).isEqualTo("#tagg8ed7");
    }

    public void testCleanupTagListWhitespace() throws Exception {
        assertThat(HereAndNowUtils.cleanupTagList("un55tagged another", '#')).isEqualTo("#un55tagged\n#another");
    }

    public void testEmptyCleanupTagList() throws Exception {
        assertThat(HereAndNowUtils.cleanupTagList("", '#')).isEqualTo("");
    }
}
