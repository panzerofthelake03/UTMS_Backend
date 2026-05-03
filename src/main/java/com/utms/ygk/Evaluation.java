package com.utms.ygk;

import com.utms.application.Application;
import com.utms.common.entity.BaseEntity;
import com.utms.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "evaluations")
public class Evaluation extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @Column(name = "composite_score", precision = 5, scale = 2)
    private BigDecimal compositeScore;

    @Column(name = "evaluator_note", columnDefinition = "TEXT")
    private String evaluatorNote;

    @Column(name = "decision", length = 50)
    private String decision;

    // YDYO-specific fields added in V4
    @Column(name = "ydyo_decision", length = 20)
    private String ydyoDecision;

    @Column(name = "ydyo_note", columnDefinition = "TEXT")
    private String ydyoNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ydyo_reviewer_id")
    private User ydyoReviewer;

    public Evaluation() {}

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public BigDecimal getCompositeScore() { return compositeScore; }
    public void setCompositeScore(BigDecimal compositeScore) { this.compositeScore = compositeScore; }

    public String getEvaluatorNote() { return evaluatorNote; }
    public void setEvaluatorNote(String evaluatorNote) { this.evaluatorNote = evaluatorNote; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getYdyoDecision() { return ydyoDecision; }
    public void setYdyoDecision(String ydyoDecision) { this.ydyoDecision = ydyoDecision; }

    public String getYdyoNote() { return ydyoNote; }
    public void setYdyoNote(String ydyoNote) { this.ydyoNote = ydyoNote; }

    public User getYdyoReviewer() { return ydyoReviewer; }
    public void setYdyoReviewer(User ydyoReviewer) { this.ydyoReviewer = ydyoReviewer; }
}
