package com.bydlauncher.domain.kbo

import androidx.compose.ui.graphics.Color

enum class KboTeam(
    val fullName: String,
    val shortName: String,
    val homeVenue: String,
    val color: Color,
) {
    KIA("KIA 타이거즈", "KIA", "광주-기아 챔피언스 필드", Color(0xFFEA0029)),
    SAMSUNG("삼성 라이온즈", "삼성", "대구 삼성 라이온즈 파크", Color(0xFF074CA1)),
    LG("LG 트윈스", "LG", "서울종합운동장 야구장", Color(0xFFC30452)),
    DOOSAN("두산 베어스", "두산", "서울종합운동장 야구장", Color(0xFF131E8C)),
    KT("KT 위즈", "KT", "수원 KT 위즈 파크", Color(0xFF444444)),
    SSG("SSG 랜더스", "SSG", "인천 SSG 랜더스필드", Color(0xFFCE0E2D)),
    LOTTE("롯데 자이언츠", "롯데", "사직 야구장", Color(0xFF0066B3)),
    HANWHA("한화 이글스", "한화", "한화생명 이글스파크", Color(0xFFF37321)),
    NC("NC 다이노스", "NC", "창원 NC 파크", Color(0xFF1D6EC5)),
    KIWOOM("키움 히어로즈", "키움", "고척 스카이돔", Color(0xFF820024)),
}
