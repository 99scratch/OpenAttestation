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
import com.intel.mtwilson.as.data.TblOs;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author dsmagadx
 */
public class TblOsJpaController implements Serializable {

    public TblOsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblOs tblOs) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(tblOs);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(TblOs tblOs) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(tblOs);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblOs.getId();
                if (findTblOs(id) == null) {
                    throw new NonexistentEntityException("The tblOs with id " + id + " no longer exists.");
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
            TblOs tblOs;
            try {
                tblOs = em.getReference(TblOs.class, id);
                tblOs.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblOs with id " + id + " no longer exists.", enfe);
            }
            em.remove(tblOs);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblOs> findTblOsEntities() {
        return findTblOsEntities(true, -1, -1);
    }

    public List<TblOs> findTblOsEntities(int maxResults, int firstResult) {
        return findTblOsEntities(false, maxResults, firstResult);
    }

    private List<TblOs> findTblOsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblOs.class));
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

    public TblOs findTblOs(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblOs.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblOsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblOs> rt = cq.from(TblOs.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public TblOs findTblOsByNameVersion(String name, String version) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblOs.findTblOsByNameVersion");
            query.setParameter("name", name);
            query.setParameter("version", version);
            
            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);

           
            TblOs tblOs = (TblOs) query.getSingleResult();
            return tblOs;

        } catch(NoResultException e){
            return null;
        }finally {
            em.close();
        }
    }
}
