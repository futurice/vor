package com.futurice.vor;

import android.test.suitebuilder.annotation.*;

import com.futurice.vor.utils.VorUtils;

import junit.framework.*;

import static org.assertj.core.api.Assertions.*;

@SmallTest
public class VorUtilsTest extends TestCase {

    public void testCleanupTagLeadingSpaces() throws Exception {
        assertThat(VorUtils.cleanupTag("   @this", '@')).isEqualTo("@this");
    }

    public void testCleanupTagTrainlingLineBreakAndSpaces() throws Exception {
        assertThat(VorUtils.cleanupTag("@this  \n  ", '@')).isEqualTo("@this");
    }

    public void testCleanupTagNoMark() throws Exception {
        assertThat(VorUtils.cleanupTag("  untagged..", '#')).isEqualTo("#untagged");
    }

    public void testCleanupTagExtraMark() throws Exception {
        assertThat(VorUtils.cleanupTag("  # # ##untagged..", '#')).isEqualTo("#untagged");
    }

    public void testCleanupTagUnsupportedInMiddle() throws Exception {
        assertThat(VorUtils.cleanupTag("un..55 tagged..", '#')).isEqualTo("#un55tagged");
    }

    public void testLeadingNumbers() throws Exception {
        assertThat(VorUtils.cleanupTag("66tagg 8 ed7..", '#')).isEqualTo("#tagg8ed7");
    }

    public void testCleanupTagListWhitespace() throws Exception {
        assertThat(VorUtils.cleanupTagList("un55tagged another", '#')).isEqualTo("#un55tagged\n#another");
    }

    public void testEmptyCleanupTagList() throws Exception {
        assertThat(VorUtils.cleanupTagList("", '#')).isEqualTo("");
    }
}
