package com.springLearnig.telegramBot.notifications.Service.alfaExchange;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vdurmont.emoji.EmojiParser;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Builder
public class AlfaEntity {

    private Double purchase;
    private Double selling;
    private Double purchasePct;
    private Double sellingPct;
    private Double mark;
    private Double markPct;

    public AlfaEntity(Double purchase, Double selling, Double purchasePct, Double sellingPct, Double mark, Double markPct) {
        this.purchase = purchase;
        this.selling = selling;
        this.purchasePct = purchasePct;
        this.sellingPct = sellingPct;
        this.mark = mark;
        this.markPct = markPct;
    }

    private final String UP = EmojiParser.parseToUnicode(":arrow_up_small:");
    private final String DOWN = EmojiParser.parseToUnicode(":small_red_triangle_down:");

    public Double getPurchase() {
        return purchase;
    }

    public void setPurchase(Double purchase) {
        this.purchase = purchase;
    }

    public Double getSelling() {
        return selling;
    }

    public void setSelling(Double selling) {
        this.selling = selling;
    }

    public Double getPurchasePct() {
        return purchasePct;
    }

    public void setPurchasePct(Double purchasePct) {
        this.purchasePct = purchasePct;
    }

    public Double getSellingPct() {
        return sellingPct;
    }

    public void setSellingPct(Double sellingPct) {
        this.sellingPct = sellingPct;
    }

    public Double getMark() {
        return mark;
    }

    public void setMark(Double mark) {
        this.mark = mark;
    }

    public Double getMarkPct() {
        return markPct;
    }

    public void setMarkPct(Double markPct) {
        this.markPct = markPct;
    }

    @JsonIgnore
    public String getText() {
        return "purchase " + purchase + "\t" +
                (purchasePct.compareTo(Double.MIN_NORMAL) > 0 ? UP + purchasePct : DOWN + purchasePct) + "%" +
                "\nselling " + selling + "\t" +
                (sellingPct.compareTo(Double.MIN_NORMAL) > 0 ? UP + sellingPct : DOWN + sellingPct) + "%" +
                "\nmark " + mark + "\t" + (markPct.compareTo(Double.MIN_NORMAL) > 0 ? UP + markPct : DOWN + markPct) + "%";
    }
}
