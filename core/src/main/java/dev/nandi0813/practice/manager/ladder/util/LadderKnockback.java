package dev.nandi0813.practice.manager.ladder.util;

import dev.nandi0813.practice.manager.ladder.enums.KnockbackType;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LadderKnockback {

    private KnockbackType knockbackType;

    public LadderKnockback() {
        knockbackType = KnockbackType.DEFAULT;
    }

    public LadderKnockback(LadderKnockback ladderKnockback) {
        super();
        this.knockbackType = ladderKnockback.knockbackType;
    }

    public void get(final String knockbackValue) {
        KnockbackType knockback;
        try {
            knockback = KnockbackType.valueOf(knockbackValue);
        } catch (IllegalArgumentException e) {
            Common.sendConsoleMMMessage("<red>Invalid knockback type: " + knockbackValue + ". Defaulting to DEFAULT.");
            knockback = KnockbackType.DEFAULT;
        }

        knockbackType = knockback;
    }

    public String get() {
        return knockbackType.toString();
    }

    public boolean isDefault() {
        return knockbackType == KnockbackType.DEFAULT;
    }

}
