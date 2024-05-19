package run.piece.dev.refactoring.ui.portfolio.detail

enum class RecruitmentState(title: String,
                            description: (timeDifference: TimeDifference?, achievementRate: String) -> String,
                            endDate: (String) -> String,
                            marketingMessage: (tapStatus: Boolean, memberCount: String) -> String,
                            gifResource: (tapStatus: Boolean, memberCount: String) -> String,
                            buttonTitle: (tapStatus: Boolean) -> String,
                            buttonTitleTapped: String,
                            buttonTime:(timeDifference: TimeDifference) -> String) {
    /**
     * RECRUITMENT_SCHEDULED : 모집예정
     * RECRUITING : 모집중
     * DEADLINE : 모집마감
     * DIVIDEND_SCHEDULED : 분배 예정
     * DIVIDEND_ENDOF : 분배 예정 - 증권 만기
     * DIVIDEND_COMPLETED : 분배 완료
     * */

    RECRUITMENT_SCHEDULED(
        title = "모집예정",
        description = { timeDifference, _ ->
            timeDifference?.let { time ->
                if (time.day == 0) {
                    "청약까지 ${time.hour}시간 ${time.minute}분 남았어요"
                    if (time.hour == 0L) {
                        "청약까지 ${time.minute}분 ${time.second}초 남았어요"

                        if (time.minute == 0L) "청약까지 ${time.second}초 남았어요"

                        else "청약까지 ${time.minute}분 ${time.second}초 남았어요"

                    } else "청약까지 ${time.hour}시간 ${time.minute}분 남았어요"

                } else "청약까지 ${time.day}일 ${time.hour}시간 남았어요"
            } ?: ""
        },
        endDate = {
            "$it 청약모집"
        },
        marketingMessage = { tapStatus, memberCount ->
            if (!tapStatus) "${memberCount}명의 회원이\n알림을 신청했어요!" else "알림 신청하고 빠르게 소식을 받아보세요!"
        },
        gifResource = { tapStatus, _ ->
            if (!tapStatus) "image_person_tri" else "portfolio_bell"
        },
        buttonTitle = { tapStatus ->
            if (!tapStatus) "알림 신청 완료" else "오픈 알림 받기"
        },
        buttonTitleTapped = "알림 신청 완료",
        buttonTime = { timeDifference ->
            if (timeDifference.day > 0) { //1일 이상
                "D-${timeDifference.day}"
            } else if (timeDifference.hour > 0) { //1시간 이상
                var timeHour = "${timeDifference.hour}"
                var timeMinute = "${timeDifference.minute}"

                if (timeDifference.hour < 10) timeHour = "0$timeHour"
                if (timeDifference.minute < 10) timeMinute = "0$timeMinute"
                "$timeHour:$timeMinute"

            } else { //59분 59초 이하
                var timeMinute = "${timeDifference.minute}"
                var timeSecond = "${timeDifference.second}"

                if (timeDifference.minute < 10) timeMinute = "0$timeMinute"
                if (timeDifference.second < 10) timeSecond = "0$timeSecond"
                "$timeMinute:$timeSecond"
            }
        }
    ),
    RECRUITING(
        title = "모집 중",
        description = { timeDifference, achievementRate ->
            if (achievementRate.isEmpty()) "0% 모집되었어요"
            else "${achievementRate}% 모집되었어요"
        },
        endDate = {
            "$it 청약마감"
        },
        marketingMessage = { tapStatus, memberCount ->
            if(memberCount.isEmpty() || memberCount == "0") {
                //if (!tapStatus) "청약 신청을\n서둘러 주세요!" else "청약 마감까지\n예치금을 유지해 주세요"
                "청약 신청을\n서둘러 주세요!"
            } else {
                //if (!tapStatus) "벌써 ${memberCount}명이\n청약을 신청했어요!" else "청약 마감까지\n예치금을 유지해 주세요"
                "벌써 ${memberCount}명이\n청약을 신청했어요!"
            }
        },
        gifResource = { tapStatus, memberCount ->
            if (memberCount.isEmpty() || memberCount == "0") {
                //if (!tapStatus) "portfolio_welcome" else "portfolio_battery"
                "portfolio_welcome"
            } else {
                //if (!tapStatus) "image_person_tri" else  "portfolio_battery"
                "image_person_tri"
            }
        },
        buttonTitle = { tapStatus ->
            if (!tapStatus) "청약 신청하기" else "청약내역 보기"
        },
        buttonTitleTapped = "",
        buttonTime = { _ -> "" }

    ),
    DEADLINE(
        title = "모집마감",
        description = { _, _ ->
            "청약이 마감되었어요"
        },
        endDate = {
            "$it 배정공시"
        },
        marketingMessage = { _, _ ->
            "배정이 끝나면\n소유권 등록을 시작해요"
        },
        gifResource = { _, _ ->
            "portfolio_clap"
        },
        buttonTitle = { _ ->
            "청약모집 마감"
        },
        buttonTitleTapped = "",
        buttonTime = { _ -> "" }

    ),
    DIVIDEND_SCHEDULED(
        title = "분배예정",
        description = { timeDifference, _ ->
            "만기까지 ${timeDifference?.day}일 남았어요"
        },
        endDate = {
            "$it 증권만기"
        },
        marketingMessage = { _, _ ->
            "수익을 위해\n노력하고 있어요"
        },
        gifResource = { _, _ ->
            "portfolio_run"
        },
        buttonTitle = { _ ->
            "수익 분배 예정"
        },
        buttonTitleTapped = "",
        buttonTime = { _ -> "" }

    ),
    DIVIDEND_ENDOF(
        title = "분배예정",
        description = { _, _ ->
            "오늘은 만기일이에요"
        },
        endDate = {
            "$it 증권만기"
        },
        marketingMessage = { _, _ ->
            "가상계좌로 분배금이 \n입금될 예정이에요"
        },
        gifResource = { _, _ ->
            "portfolio_bank_book"
        },
        buttonTitle = { _ ->
            "수익 분배 예정"
        },
        buttonTitleTapped = "",
        buttonTime = { _ -> "" }
    ),
    DIVIDEND_COMPLETED(
        title = "분배완료",
        description = { _, achievementRate ->
            if (achievementRate.isEmpty()) "0% 수익을 달성했어요"
            else "${achievementRate}% 수익을 달성했어요"
        },
        endDate = {
            "$it 분배완료"
        },
        marketingMessage = { _, _ ->
            "분배금 지급이\n완료되었어요"
        },
        gifResource = { _, _ ->
            "portfolio_bank_book"
        },
        buttonTitle = { _ ->
            "수익 분배 완료"
        },
        buttonTitleTapped = "",
        buttonTime = { _ -> "" }
    );

    val title: String
    val description: (timeDifference: TimeDifference?, achievementRate: String) -> String
    val endDate: (String) -> String
    val marketingMessage: (tapStatus: Boolean, memberCount: String) -> String
    val gifResource: (tapStatus: Boolean, memberCount: String) -> String
    val buttonTitle: (tapStatus: Boolean) -> String
    val buttonTitleTapped: String
    val buttonTime: (timeDifference: TimeDifference) -> String
    init {
        this.title = title
        this.description = description
        this.endDate = endDate
        this.marketingMessage = marketingMessage
        this.gifResource = gifResource
        this.buttonTitle = buttonTitle
        this.buttonTitleTapped = buttonTitleTapped
        this.buttonTime = buttonTime
    }
}

