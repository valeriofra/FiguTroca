package com.figutroca.app.data

/**
 * Valerio's real Panini World Cup 2026 collection, used to pre-fill the app on
 * first launch. Two lists in the collector format parsed by [com.figutroca.app.util.ListParser]:
 *
 *  - [REPETIDAS]: spare stickers for trading. A number's "(n)" is how many
 *    spares; total copies owned = spares + 1.
 *  - [FALTAM]: stickers still missing to complete the album.
 *
 * Seeding rule (see [Repository.ensureActiveCollection]): for every team, any
 * number that is neither missing nor a spare counts as owned (1 copy). Special
 * sections (FWC, CC) only include the numbers that appear in these lists,
 * because their full size isn't confirmed yet.
 *
 * Notes on the source data:
 *  - "BRA 1(21)" was treated as "BRA 1" (a single copy); 21 spares looked like a typo.
 *  - "JUN" (spares 5, 6(2), 11, 14) was left out — likely a typo for another
 *    code (possibly TUN); left pending confirmation.
 */
object SeedData {

    const val REPETIDAS = """
        FWC 1(2), 12, 11, 12, 15, 18
        MEX 4(2), 8, 9, 12, 14, 17, 18, 20
        RSA 1, 2(2), 3(2), 14, 9, 13(2), 18, 20
        KOR 11, 12, 13, 14, 15, 16, 17, 20
        CZE 3, 18
        CAN 2, 10, 11, 16
        BIH 3, 5, 13, 16
        QAT 1(2), 2, 3, 5, 6, 7, 8(2), 9, 10, 13
        SUI 2, 3(2), 6, 8(2), 12, 14(2), 16(2), 17
        BRA 2, 3, 4(2), 14, 15, 1, 9, 12, 14
        MAR 3, 11, 15(2), 16, 19, 20(2)
        HAI 1, 2, 10, 14, 15, 17, 20
        SCO 2, 9, 10, 20
        USA 3, 6, 8(2), 11, 12
        PAR 6, 10, 19
        AUS 1, 4(2), 8, 12, 14, 17(2), 20(2)
        TUR 4, 6, 11, 20
        GER 16
        CUW 3, 7
        CIV 1, 2, 3, 10, 18
        ECU 1, 17, 19, 20
        NED 2, 3, 5, 6, 7(2), 10, 11, 13(2), 14, 15, 16, 20
        JPN 5, 9
        SWE 6(2), 7, 8, 11
        BEL 2, 3, 4, 8, 9, 12, 13
        EGY 5, 6, 7, 9, 10(2), 11, 12, 16, 20
        IRN 1, 5(2), 9(2), 11, 14(2), 18
        ESP 2, 3, 7, 11, 16, 20
        CPV 3(2), 6, 7(2), 10, 11, 13, 16, 19
        KSA 3, 4, 5, 9, 10, 16, 17
        URU 4, 5, 9, 14, 18
        FRA 2, 12, 14(2), 17(2), 18, 20
        SEN 6, 8, 16(2), 17(2), 18
        IRQ 4, 7, 12, 13
        NOR 11
        ARG 6, 9, 18
        ALG 3, 4, 16
        AUT 2, 5, 6(3), 8, 11, 16, 19(2), 20
        JOR 2, 3
        POR 7, 8(2), 9, 12(2), 14(2), 16, 17(2), 18
        COD 7, 13
        UZB 3, 14, 18, 20
        COL 4, 5, 8, 12, 13(2), 14(2), 15, 16, 20
        ENG 9, 12, 13, 14, 18(2)
        CRO 8, 12(2)
        GHA 17, 18
        PAN 18
    """

    const val FALTAM = """
        FWC 4, 7, 9, 10, 14, 16, 17
        CC 1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14
        RSA 6, 7, 10, 11, 15, 16
        KOR 1, 5
        CZE 1, 2, 7, 8, 10, 17, 19
        QAT 4, 11, 16, 20
        CAN 5, 7, 9, 12, 13, 14, 17, 20
        BIH 2, 4, 6, 7, 9, 15, 17
        SUI 1, 4, 20
        BRA 8, 9, 12, 13, 16, 17, 20
        MAR 1, 5, 6, 9, 12
        HAI 4, 8, 12
        SCO 4, 8, 9, 10, 14, 17
        USA 7, 17, 18, 19, 20
        PAR 4, 5, 13, 16, 18, 20
        AUS 2, 3, 6, 10, 15
        TUR 3, 7, 8, 9, 10, 14, 19
        GER 4, 5, 6, 8, 9, 10, 12, 14, 15, 20
        CUW 1, 4, 6, 8, 9, 10, 12, 15, 17, 19
        CIV 4, 5, 8, 9, 11, 12
        ECU 2, 3, 9, 17
        NED 4, 8, 12
        JPN 1, 6, 7, 8, 10, 11, 12, 13, 15, 16, 20
        SWE 3, 4, 13, 19, 20
        TUN 1, 2, 3, 7, 9, 10, 13, 15, 16, 17, 19
        BEL 10, 11, 14, 16, 17, 18, 19, 20
        EGY 1, 3, 8
        IRN 10, 13, 19, 20
        NZL 4, 5, 8, 12, 13, 14, 17, 18
        URU 5, 6, 9, 13, 15, 16
        KSA 1, 2, 3, 6, 7, 11, 13, 20
        CPV 3, 12, 20
        ESP 4, 8, 9, 14
        FRA 1, 3, 7, 10, 16, 17, 19, 20
        SEN 11, 15
        IRQ 5, 11, 16, 18
        NOR 1, 13, 19, 20
        POR 2, 3, 5, 10, 13, 15, 19
        COD 3, 6, 8, 9, 11, 12, 14, 18, 20
        UZB 1, 2, 4, 6, 12, 16, 17
        COL 2, 6, 10, 18
        ENG 3, 7, 15, 16, 19
        CRO 4, 5, 6, 7, 10, 11, 13, 15, 16, 17, 18, 19, 20
        GHA 1, 5, 9, 13, 14, 19
        PAN 1, 3, 4, 6, 7, 11, 16, 19, 20
        ARG 4, 5, 8, 11, 12, 14, 20
        ALG 1, 2, 5, 6, 10, 19
        AUT 3, 4, 12
        JOR 1, 2, 9, 14, 15
    """
}
