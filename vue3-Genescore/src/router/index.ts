import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import GeneDetailView from '../views/GeneDetailView.vue'
import GeneSearchView from '../views/GeneSearchView.vue'
import DkoView from '../views/DkoView.vue'
import AnalysisView from '../views/AnalysisView.vue'
import ApiDownloadsView from '../views/ApiDownloadsView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/search',
      name: 'gene-search',
      component: GeneSearchView
    },
    {
      path: '/gene/:symbol',
      name: 'gene-detail',
      component: GeneDetailView
    },
    {
      path: '/dko',
      name: 'dko',
      component: DkoView
    },
    {
      path: '/analysis',
      name: 'analysis',
      component: AnalysisView
    },
    {
      path: '/browse',
      name: 'browse',
      component: HomeView
    },
    {
      path: '/api',
      name: 'api',
      component: ApiDownloadsView
    },
    {
      path: '/downloads',
      name: 'downloads',
      component: ApiDownloadsView
    }
  ]
})

export default router