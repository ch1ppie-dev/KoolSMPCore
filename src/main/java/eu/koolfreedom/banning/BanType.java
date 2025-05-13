package eu.koolfreedom.banning;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BanType
{
    QUICKBAN(300000),
    BAN(86400000);

    @Getter
    public final int length;
}
