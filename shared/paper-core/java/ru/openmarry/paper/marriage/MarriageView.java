package ru.openmarry.paper.marriage;

import java.util.UUID;

public interface MarriageView {

    UUID partnerOf(UUID player);

    String heartColorCode(UUID player);
}
