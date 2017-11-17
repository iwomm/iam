package it.infn.mw.iam.persistence.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "iam_aup")
public class IamAup implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(name = "name", nullable = false, length = 36, unique = true)
  String name;

  @Column(name = "description", length = 128)
  String description;

  @Column(name = "sig_validity_days", nullable = false)
  Long signatureValidityInDays;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "creation_time", nullable = false)
  Date creationTime;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_update_time", nullable = false)
  Date lastUpdateTime;

  @Lob
  @Column(name="text", nullable=false)
  String text;

  public IamAup() {
    // empty constructor
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }



  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IamAup other = (IamAup) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }



  public Long getId() {
    return id;
  }


  public void setId(Long id) {
    this.id = id;
  }


  public String getName() {
    return name;
  }


  public void setName(String name) {
    this.name = name;
  }


  public String getDescription() {
    return description;
  }


  public void setDescription(String description) {
    this.description = description;
  }


  public Long getSignatureValidityInDays() {
    return signatureValidityInDays;
  }


  public void setSignatureValidityInDays(Long signatureValidityInDays) {
    this.signatureValidityInDays = signatureValidityInDays;
  }


  public Date getCreationTime() {
    return creationTime;
  }


  public void setCreationTime(Date creationTime) {
    this.creationTime = creationTime;
  }


  public Date getLastUpdateTime() {
    return lastUpdateTime;
  }


  public void setLastUpdateTime(Date lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

}
