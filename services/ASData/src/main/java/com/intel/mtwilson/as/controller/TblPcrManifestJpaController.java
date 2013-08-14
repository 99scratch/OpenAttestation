/*
 * Copyright (c) 2013, Intel Corporation. 
 * All rights reserved.
 * 
 * The contents of this file are released under the BSD license, you may not use this file except in compliance with the License.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblPcrManifest;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
//import com.intel.mtwilson.as.data.TblDbPortalUser;
import com.intel.mtwilson.as.data.TblMle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author dsmagadx
 */
public class TblPcrManifestJpaController implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());

    public TblPcrManifestJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        EntityManager em = emf.createEntityManager();
        return em;
    }

    public void create(TblPcrManifest tblPcrManifest) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            TblDbPortalUser updatedBy = tblPcrManifest.getUpdatedBy();
            if (updatedBy != null) {
                updatedBy = em.getReference(updatedBy.getClass(), updatedBy.getId());
                tblPcrManifest.setUpdatedBy(updatedBy);
            }
            TblDbPortalUser createdBy = tblPcrManifest.getCreatedBy();
            if (createdBy != null) {
                createdBy = em.getReference(createdBy.getClass(), createdBy.getId());
                tblPcrManifest.setCreatedBy(createdBy);
            }*/
            TblMle mleId = tblPcrManifest.getMleId();
            if (mleId != null) {
                mleId = em.getReference(mleId.getClass(), mleId.getId());
                tblPcrManifest.setMleId(mleId);
            }
            em.persist(tblPcrManifest);
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            if (updatedBy != null) {
                updatedBy.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(updatedBy);
            }
            if (createdBy != null) {
                createdBy.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(createdBy);
            }*/
            if (mleId != null) {
                mleId.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(mleId);
            }
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(TblPcrManifest tblPcrManifest) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblPcrManifest persistentTblPcrManifest = em.find(TblPcrManifest.class, tblPcrManifest.getId());
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            TblDbPortalUser updatedByOld = persistentTblPcrManifest.getUpdatedBy();
            TblDbPortalUser updatedByNew = tblPcrManifest.getUpdatedBy();
            TblDbPortalUser createdByOld = persistentTblPcrManifest.getCreatedBy();
            TblDbPortalUser createdByNew = tblPcrManifest.getCreatedBy();
            */
            TblMle mleIdOld = persistentTblPcrManifest.getMleId();
            TblMle mleIdNew = tblPcrManifest.getMleId();
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            if (updatedByNew != null) {
                updatedByNew = em.getReference(updatedByNew.getClass(), updatedByNew.getId());
                tblPcrManifest.setUpdatedBy(updatedByNew);
            }
            if (createdByNew != null) {
                createdByNew = em.getReference(createdByNew.getClass(), createdByNew.getId());
                tblPcrManifest.setCreatedBy(createdByNew);
            }*/
            if (mleIdNew != null) {
                mleIdNew = em.getReference(mleIdNew.getClass(), mleIdNew.getId());
                tblPcrManifest.setMleId(mleIdNew);
            }
            tblPcrManifest = em.merge(tblPcrManifest);
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            if (updatedByOld != null && !updatedByOld.equals(updatedByNew)) {
                updatedByOld.getTblPcrManifestCollection().remove(tblPcrManifest);
                updatedByOld = em.merge(updatedByOld);
            }
            if (updatedByNew != null && !updatedByNew.equals(updatedByOld)) {
                updatedByNew.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(updatedByNew);
            }
            if (createdByOld != null && !createdByOld.equals(createdByNew)) {
                createdByOld.getTblPcrManifestCollection().remove(tblPcrManifest);
                createdByOld = em.merge(createdByOld);
            }
            if (createdByNew != null && !createdByNew.equals(createdByOld)) {
                createdByNew.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(createdByNew);
            }
            */
            if (mleIdOld != null && !mleIdOld.equals(mleIdNew)) {
                mleIdOld.getTblPcrManifestCollection().remove(tblPcrManifest);
                mleIdOld = em.merge(mleIdOld);
            }
            if (mleIdNew != null && !mleIdNew.equals(mleIdOld)) {
                mleIdNew.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(mleIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblPcrManifest.getId();
                if (findTblPcrManifest(id) == null) {
                    throw new NonexistentEntityException("The tblPcrManifest with id " + id + " no longer exists.");
                }
            }
            throw new ASDataException(ex);
        } finally {
                em.close();
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
    	EntityManager em = getEntityManager();
    	try {
            em.getTransaction().begin();
            TblPcrManifest tblPcrManifest;
            try {
                tblPcrManifest = em.getReference(TblPcrManifest.class, id);
                tblPcrManifest.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblPcrManifest with id " + id + " no longer exists.", enfe);
            }
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            TblDbPortalUser updatedBy = tblPcrManifest.getUpdatedBy();
            if (updatedBy != null) {
                updatedBy.getTblPcrManifestCollection().remove(tblPcrManifest);
                em.merge(updatedBy);
            }
            TblDbPortalUser createdBy = tblPcrManifest.getCreatedBy();
            if (createdBy != null) {
                createdBy.getTblPcrManifestCollection().remove(tblPcrManifest);
                em.merge(createdBy);
            }
            */
            TblMle mleId = tblPcrManifest.getMleId();
            if (mleId != null) {
                mleId.getTblPcrManifestCollection().remove(tblPcrManifest);
                em.merge(mleId);
            }
            em.remove(tblPcrManifest);
            em.getTransaction().commit();
    	}
    	finally {
            em.close();        		
    	}
    }

    public List<TblPcrManifest> findTblPcrManifestEntities() {
        return findTblPcrManifestEntities(true, -1, -1);
    }

    public List<TblPcrManifest> findTblPcrManifestEntities(int maxResults, int firstResult) {
        return findTblPcrManifestEntities(false, maxResults, firstResult);
    }

    private List<TblPcrManifest> findTblPcrManifestEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblPcrManifest.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public TblPcrManifest findTblPcrManifest(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblPcrManifest.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblPcrManifestCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblPcrManifest> rt = cq.from(TblPcrManifest.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public TblPcrManifest findPcrManifestById(Integer id) {
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblPcrManifest.findById");
            query.setParameter("id", id);

            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);

            TblPcrManifest manifest = (TblPcrManifest) query.getSingleResult();
            return manifest;

        } finally {
            em.close();
        }
    }
    

    /**
     * Added By: Sudhir on June 20, 2012
     * 
     * This method checks if the specified pcr entry already exists for the MLE.
     * @param id: Identity of the MLE
     * @param pcrName: Name of the PCR
     * @return : Single row result if there is a match or else null.
     */
    public TblPcrManifest findByMleIdName(Integer id, String pcrName) {
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblPcrManifest.findByMleIdName");
            query.setParameter("mleId", id);
            query.setParameter("name", pcrName);

            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);

            TblPcrManifest pcrManifest = (TblPcrManifest) query.getSingleResult();
            return pcrManifest;

        } catch(NoResultException e){
        	log.error(String.format("PCR Manifest for MLE %d PCR#  not found in Database ", id, pcrName));
        	return null;
        } finally {
            em.close();
        }               
    }

}
