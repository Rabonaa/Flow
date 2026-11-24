'use strict';

const etat = {
    session: null,
    comptes: [],
    categories: [],
    sources: [],
    devises: [],
    compteOuvert: null,
    periode: 3,
    flux: []
};

const $ = (selecteur) => document.querySelector(selecteur);

const MOINS = '−';
const NS_SVG = 'http://www.w3.org/2000/svg';
const TYPES_COMPTE = { COURANT: 'Courant', EPARGNE: 'Épargne', ESPECES: 'Espèces' };
const TITRES_ERREUR = {
    400: 'Demande invalide',
    401: 'Non connecté',
    403: 'Accès refusé',
    404: 'Introuvable',
    409: 'Contrainte non respectée',
    422: 'Règle métier non respectée',
    500: 'Erreur de la base de données'
};

/* ------------------------------------------------------------------ */
/* Thème clair / sombre                                                */
/* ------------------------------------------------------------------ */
/* Le thème initial est posé par le script de l'en-tête de index.html
   (choix mémorisé, sinon réglage du système). Ici : bascule et suivi. */

const CLE_THEME = 'flow-theme';

function themeCourant() {
    return document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light';
}

function themeMemorise() {
    try {
        const choix = localStorage.getItem(CLE_THEME);
        return choix === 'dark' || choix === 'light' ? choix : null;
    } catch (e) {
        return null;
    }
}

function appliquerTheme(theme) {
    const sombre = theme === 'dark';
    document.documentElement.dataset.theme = theme;

    const meta = document.querySelector('meta[name="theme-color"]');
    if (meta) {
        meta.setAttribute('content', sombre ? '#0A0D22' : '#2432E0');
    }
    const libelle = sombre ? 'Passer en mode clair' : 'Passer en mode sombre';
    document.querySelectorAll('.bouton-theme').forEach((bouton) => {
        bouton.setAttribute('aria-label', libelle);
        bouton.title = libelle;
    });
}

function basculerTheme() {
    const suivant = themeCourant() === 'dark' ? 'light' : 'dark';
    appliquerTheme(suivant);
    try {
        localStorage.setItem(CLE_THEME, suivant);
    } catch (e) {
        /* stockage indisponible : le choix vaut jusqu'au rechargement */
    }
}

function initialiserTheme() {
    appliquerTheme(themeCourant());
    document.querySelectorAll('.bouton-theme').forEach((bouton) => {
        bouton.addEventListener('click', basculerTheme);
    });

    // Tant que la personne n'a pas choisi, le thème suit le système en direct.
    if (window.matchMedia) {
        const systeme = window.matchMedia('(prefers-color-scheme: dark)');
        const suivre = (e) => {
            if (themeMemorise() === null) {
                appliquerTheme(e.matches ? 'dark' : 'light');
            }
        };
        if (systeme.addEventListener) {
            systeme.addEventListener('change', suivre);
        } else if (systeme.addListener) {
            systeme.addListener(suivre);
        }
    }
}

/* ------------------------------------------------------------------ */
/* Appels d'API                                                        */
/* ------------------------------------------------------------------ */

class ErreurApi extends Error {
    constructor(message, statut) {
        super(message);
        this.statut = statut;
    }
}

async function api(methode, url, corps) {
    const options = { method: methode, headers: {} };
    if (corps !== undefined) {
        options.headers['Content-Type'] = 'application/json';
        options.body = JSON.stringify(corps);
    }

    let reponse;
    try {
        reponse = await fetch(url, options);
    } catch (e) {
        throw new ErreurApi('Le serveur est injoignable. Vérifiez votre connexion puis réessayez.', 0);
    }

    const texte = await reponse.text();
    let donnees = null;
    if (texte) {
        try {
            donnees = JSON.parse(texte);
        } catch (e) {
            donnees = null;
        }
    }

    if (!reponse.ok) {
        if (reponse.status === 401 && etat.session) {
            etat.session = null;
            afficherConnexion();
        }
        throw new ErreurApi(donnees && donnees.erreur ? donnees.erreur : 'Erreur ' + reponse.status, reponse.status);
    }
    return donnees;
}

/* ------------------------------------------------------------------ */
/* Messages                                                            */
/* ------------------------------------------------------------------ */

function mouvementReduit() {
    return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

function afficherMessage(texte, type, titre) {
    const zone = $('#message');
    $(etat.session ? '#hote-message-contenu' : '#hote-message-connexion').appendChild(zone);
    zone.className = 'message ' + type;
    zone.setAttribute('role', type === 'erreur' ? 'alert' : 'status');
    $('#message-titre').textContent = titre || '';
    $('#message-texte').textContent = texte;
    zone.hidden = false;
    clearTimeout(afficherMessage.minuteur);
    if (type === 'succes') {
        afficherMessage.minuteur = setTimeout(masquerMessage, 5000);
    }
}

function masquerMessage() {
    clearTimeout(afficherMessage.minuteur);
    $('#message').hidden = true;
}

// Les erreurs de l'API (triggers, procédures) sont affichées telles quelles et restent
// à l'écran jusqu'à leur fermeture. Les confirmations disparaissent seules.
async function executer(action, messageSucces, titreErreur) {
    try {
        const resultat = await action();
        if (messageSucces) {
            afficherMessage(messageSucces, 'succes');
        }
        return resultat;
    } catch (e) {
        const titre = titreErreur || (e instanceof ErreurApi
            ? (e.statut === 0 ? 'Connexion au serveur impossible' : TITRES_ERREUR[e.statut] || 'Erreur')
            : 'Erreur');
        afficherMessage(e.message, 'erreur', titre);
        return undefined;
    }
}

function confirmer(titre, detail, libelleAction) {
    const dialogue = $('#dialogue-confirmation');
    $('#dialogue-titre').textContent = titre;
    $('#dialogue-detail').textContent = detail;
    $('#dialogue-confirmer').textContent = libelleAction || 'Supprimer';
    dialogue.returnValue = '';
    dialogue.showModal();
    return new Promise((resolve) => {
        dialogue.addEventListener('close', () => resolve(dialogue.returnValue === 'confirmer'), { once: true });
    });
}

/* ------------------------------------------------------------------ */
/* Formats                                                             */
/* ------------------------------------------------------------------ */

function montant(valeur, code) {
    const nombre = Number(valeur);
    try {
        return nombre.toLocaleString('fr-FR', { style: 'currency', currency: code || 'EUR' });
    } catch (e) {
        return nombre.toFixed(2) + ' ' + (code || '');
    }
}

function montantSigne(valeur, code, positif) {
    return (positif ? '+ ' : MOINS + ' ') + montant(Math.abs(Number(valeur)), code);
}

function montantCompact(valeur, code) {
    try {
        return Number(valeur).toLocaleString('fr-FR', {
            style: 'currency',
            currency: code || 'EUR',
            notation: 'compact',
            maximumFractionDigits: 1
        });
    } catch (e) {
        return String(Math.round(Number(valeur)));
    }
}

function symboleDevise(code) {
    try {
        const parties = new Intl.NumberFormat('fr-FR', { style: 'currency', currency: code }).formatToParts(0);
        const symbole = parties.find((p) => p.type === 'currency');
        return symbole ? symbole.value : code;
    } catch (e) {
        return code || '';
    }
}

function dateFr(valeur) {
    if (!valeur) {
        return '';
    }
    const [a, m, j] = String(valeur).substring(0, 10).split('-');
    return j + '/' + m + '/' + a;
}

function moisFr(valeur) {
    const [a, m] = String(valeur).split('-');
    const date = new Date(Number(a), Number(m) - 1, 1);
    const texte = date.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' });
    return texte.charAt(0).toUpperCase() + texte.slice(1);
}

function moisCourt(valeur) {
    const [a, m] = String(valeur).split('-');
    return new Date(Number(a), Number(m) - 1, 1).toLocaleDateString('fr-FR', { month: 'short', year: '2-digit' });
}

function pourcentage(valeur) {
    return Number(valeur).toLocaleString('fr-FR', { minimumFractionDigits: 1, maximumFractionDigits: 1 }) + ' %';
}

function codeDevise(idDevise) {
    const devise = etat.devises.find((d) => d.idDevise === idDevise);
    return devise ? devise.code : '';
}

function devisesDuCompte(idCompte) {
    const ligne = etat.comptes.find((l) => l.compte.idCompte === idCompte);
    return ligne ? codeDevise(ligne.compte.idDevise) : 'EUR';
}

/* ------------------------------------------------------------------ */
/* Aides DOM                                                           */
/* ------------------------------------------------------------------ */

function cellule(texte, classe, etiquette) {
    const td = document.createElement('td');
    td.textContent = texte;
    if (classe) {
        td.className = classe;
    }
    if (etiquette) {
        td.dataset.etiquette = etiquette;
    }
    return td;
}

function bouton(texte, classe, action, libelleAria) {
    const b = document.createElement('button');
    b.type = 'button';
    b.textContent = texte;
    b.className = classe;
    if (libelleAria) {
        b.setAttribute('aria-label', libelleAria);
    }
    b.addEventListener('click', action);
    return b;
}

function ligneVide(tbody, nbColonnes, texte) {
    const tr = document.createElement('tr');
    const td = cellule(texte, 'vide');
    td.colSpan = nbColonnes;
    tr.appendChild(td);
    tbody.appendChild(tr);
}

function remplirSelect(select, elements, valeur, libelle) {
    select.replaceChildren();
    for (const element of elements) {
        const option = document.createElement('option');
        option.value = valeur(element);
        option.textContent = libelle(element);
        select.appendChild(option);
    }
}

function creerTable(classe, colonnes) {
    const table = document.createElement('table');
    table.className = classe;
    const thead = document.createElement('thead');
    const tr = document.createElement('tr');
    for (const c of colonnes) {
        const th = document.createElement('th');
        th.scope = 'col';
        th.textContent = c.titre;
        if (c.nombre) {
            th.className = 'nombre';
        }
        tr.appendChild(th);
    }
    thead.appendChild(tr);
    table.appendChild(thead);
    const tbody = document.createElement('tbody');
    table.appendChild(tbody);
    return { table, tbody };
}

function noeudSvg(nom, attributs) {
    const noeud = document.createElementNS(NS_SVG, nom);
    for (const [cle, valeur] of Object.entries(attributs || {})) {
        noeud.setAttribute(cle, valeur);
    }
    return noeud;
}

function texteSvg(contenu, attributs) {
    const t = noeudSvg('text', attributs);
    t.textContent = contenu;
    return t;
}

/* ------------------------------------------------------------------ */
/* Navigation                                                          */
/* ------------------------------------------------------------------ */

function activerOnglets(nav, section) {
    const boutons = nav.querySelectorAll('button');
    const onglets = section.querySelectorAll('.onglet');
    boutons.forEach((b) => {
        b.addEventListener('click', () => {
            boutons.forEach((x) => {
                if (x === b) {
                    x.setAttribute('aria-current', 'page');
                } else {
                    x.removeAttribute('aria-current');
                }
            });
            onglets.forEach((o) => { o.hidden = o.id !== b.dataset.onglet; });
            masquerMessage();
            window.scrollTo(0, 0);
            const chargement = chargements[b.dataset.onglet];
            if (chargement) {
                chargement();
            }
        });
    });
}

function ouvrirPremierOnglet(nav) {
    nav.querySelector('button').click();
}

function afficherConnexion() {
    $('#coque').hidden = true;
    $('#ecran-utilisateur').hidden = true;
    $('#ecran-admin').hidden = true;
    $('#ecran-connexion').hidden = false;
}

async function afficherApplication() {
    const admin = etat.session.role === 'ADMIN';

    $('#ecran-connexion').hidden = true;
    $('#coque').hidden = false;
    $('#zone-utilisateur').hidden = false;
    $('#nom-utilisateur').textContent = etat.session.pseudo;
    $('#role-utilisateur').textContent = admin ? 'Administrateur' : 'Utilisateur';
    $('#nav-admin').hidden = !admin;
    $('#nav-utilisateur').hidden = admin;

    etat.categories = await api('GET', '/api/categories');

    if (admin) {
        $('#ecran-utilisateur').hidden = true;
        $('#ecran-admin').hidden = false;
        ouvrirPremierOnglet($('#nav-admin'));
    } else {
        etat.sources = await api('GET', '/api/sources');
        etat.devises = await api('GET', '/api/devises');
        remplirSelect($('#saisie-source'), etat.sources, (s) => s.idSource, (s) => s.nom);
        remplirSelect($('#compte-devise'), etat.devises, (d) => d.idDevise, (d) => d.code);
        $('#ecran-admin').hidden = true;
        $('#ecran-utilisateur').hidden = false;
        ouvrirPremierOnglet($('#nav-utilisateur'));
    }
}

/* ------------------------------------------------------------------ */
/* Utilisateur : comptes                                               */
/* ------------------------------------------------------------------ */

function afficherTotaux() {
    const parDevise = new Map();
    for (const ligne of etat.comptes) {
        const code = codeDevise(ligne.compte.idDevise) || 'EUR';
        if (!parDevise.has(code)) {
            parDevise.set(code, { code, centimes: 0, nb: 0 });
        }
        const total = parDevise.get(code);
        total.centimes += Math.round(Number(ligne.solde) * 100);
        total.nb += 1;
    }

    const conteneur = $('#totaux');
    conteneur.replaceChildren();
    $('#aplat-total').hidden = parDevise.size === 0;

    const totaux = [...parDevise.values()].sort((a, b) => b.nb - a.nb || a.code.localeCompare(b.code));
    for (const t of totaux) {
        const bloc = document.createElement('div');
        bloc.className = 'total';
        const valeur = document.createElement('p');
        valeur.className = 'total-montant';
        valeur.textContent = montant(t.centimes / 100, t.code);
        const detail = document.createElement('p');
        detail.className = 'total-detail';
        detail.textContent = t.nb + (t.nb > 1 ? ' comptes en ' : ' compte en ') + t.code;
        bloc.appendChild(valeur);
        bloc.appendChild(detail);
        conteneur.appendChild(bloc);
    }
}

async function chargerComptes() {
    etat.comptes = await api('GET', '/api/comptes');
    afficherTotaux();

    const tbody = $('#table-comptes tbody');
    tbody.replaceChildren();

    if (etat.comptes.length === 0) {
        ligneVide(tbody, 4, 'Aucun compte pour le moment. Ouvrez votre premier compte ci-dessous.');
    }

    for (const ligne of etat.comptes) {
        const c = ligne.compte;
        const tr = document.createElement('tr');
        tr.dataset.idCompte = c.idCompte;
        if (etat.compteOuvert === c.idCompte) {
            tr.classList.add('selectionne');
        }
        tr.appendChild(cellule(c.nom, 'c-titre'));
        tr.appendChild(cellule(TYPES_COMPTE[c.type] || c.type, 'c-sous'));
        tr.appendChild(cellule(montant(ligne.solde, codeDevise(c.idDevise)), 'nombre c-solde'));
        const actions = document.createElement('td');
        actions.className = 'nombre c-actions';
        actions.appendChild(bouton('Opérations', 'bouton contour petit', () => chargerTransactions(c.idCompte, true),
            'Voir les opérations du compte ' + c.nom));
        actions.appendChild(bouton('Supprimer', 'bouton danger petit', () => supprimerCompte(c),
            'Supprimer le compte ' + c.nom));
        tr.appendChild(actions);
        tbody.appendChild(tr);
    }

    const select = $('#saisie-compte');
    const precedent = select.value;
    remplirSelect(select, etat.comptes, (l) => l.compte.idCompte,
        (l) => l.compte.nom + ' (' + montant(l.solde, codeDevise(l.compte.idDevise)) + ')');
    if (precedent && etat.comptes.some((l) => String(l.compte.idCompte) === precedent)) {
        select.value = precedent;
    }
    majSuffixeDevise();

    if (etat.compteOuvert !== null && etat.comptes.some((l) => l.compte.idCompte === etat.compteOuvert)) {
        await chargerTransactions(etat.compteOuvert, false);
    } else {
        etat.compteOuvert = null;
        $('#bloc-transactions').hidden = true;
    }
}

async function chargerTransactions(idCompte, defiler) {
    if (defiler) {
        masquerMessage();
    }
    const transactions = await executer(() => api('GET', '/api/comptes/' + idCompte + '/transactions'));
    if (!transactions) {
        return;
    }
    etat.compteOuvert = idCompte;
    const ligne = etat.comptes.find((l) => l.compte.idCompte === idCompte);
    const code = codeDevise(ligne.compte.idDevise);

    document.querySelectorAll('#table-comptes tbody tr[data-id-compte]').forEach((tr) => {
        const choisi = Number(tr.dataset.idCompte) === idCompte;
        tr.classList.toggle('selectionne', choisi);
        if (choisi) {
            tr.setAttribute('aria-current', 'true');
        } else {
            tr.removeAttribute('aria-current');
        }
    });

    $('#titre-transactions').textContent = 'Opérations du compte « ' + ligne.compte.nom + ' » (' + transactions.length + ')';
    const tbody = $('#table-transactions tbody');
    tbody.replaceChildren();

    if (transactions.length === 0) {
        ligneVide(tbody, 5, 'Aucune opération sur ce compte. Utilisez « Nouvelle opération » pour en ajouter une.');
    }

    for (const t of transactions) {
        const tr = document.createElement('tr');
        const estRevenu = t.type === 'REVENU';

        tr.appendChild(cellule(dateFr(t.date), 'c-sous c-date'));

        const libelle = document.createElement('td');
        libelle.className = 'c-titre';
        const nom = document.createElement('span');
        nom.className = 'libelle';
        nom.textContent = t.nom;
        libelle.appendChild(nom);
        if (t.description) {
            const description = document.createElement('span');
            description.className = 'description';
            description.textContent = t.description;
            libelle.appendChild(description);
        }
        tr.appendChild(libelle);

        tr.appendChild(cellule(estRevenu ? 'Revenu' : 'Dépense', 'c-sous c-type'));
        tr.appendChild(cellule(montantSigne(t.montant, code, estRevenu),
            'nombre c-valeur ' + (estRevenu ? 'revenu' : 'depense')));

        const actions = document.createElement('td');
        actions.className = 'nombre c-actions';
        actions.appendChild(bouton('Supprimer', 'bouton danger petit', () => supprimerTransaction(t),
            'Supprimer l\'opération ' + t.nom));
        tr.appendChild(actions);
        tbody.appendChild(tr);
    }

    $('#bloc-transactions').hidden = false;
    if (defiler) {
        $('#titre-transactions').scrollIntoView({ behavior: mouvementReduit() ? 'auto' : 'smooth', block: 'start' });
    }
}

async function supprimerTransaction(t) {
    if (!await confirmer('Supprimer l\'opération « ' + t.nom + ' » ?', 'Cette action est définitive.')) {
        return;
    }
    masquerMessage();
    const ok = await executer(() => api('DELETE', '/api/transactions/' + t.idTransaction), 'Opération supprimée.');
    if (ok !== undefined) {
        await executer(chargerComptes);
    }
}

async function supprimerCompte(c) {
    if (!await confirmer('Supprimer le compte « ' + c.nom + ' » ?', 'Cette action est définitive.')) {
        return;
    }
    masquerMessage();
    const ok = await executer(() => api('DELETE', '/api/comptes/' + c.idCompte), 'Compte supprimé.');
    if (ok !== undefined) {
        await executer(chargerComptes);
    }
}

async function creerCompte(evenement) {
    evenement.preventDefault();
    masquerMessage();
    const f = evenement.target;
    const corps = {
        nom: f.nom.value.trim(),
        type: f.type.value,
        montantInitial: Number(f.montantInitial.value),
        idDevise: Number(f.idDevise.value)
    };
    const ok = await executer(() => api('POST', '/api/comptes', corps), 'Compte créé.');
    if (ok !== undefined) {
        f.reset();
        f.closest('details').open = false;
        await executer(chargerComptes);
    }
}

/* ------------------------------------------------------------------ */
/* Utilisateur : nouvelle opération                                    */
/* ------------------------------------------------------------------ */

function majSuffixeDevise() {
    const idCompte = Number($('#saisie-compte').value);
    $('#saisie-devise').textContent = symboleDevise(devisesDuCompte(idCompte));
    majResumeRepartition();
}

function majResumeRepartition() {
    const zone = $('#resume-repartition');
    const lignes = [...$('#lignes-repartition').children];
    const code = devisesDuCompte(Number($('#saisie-compte').value));
    const total = Number($('#form-saisie').montant.value) || 0;

    zone.classList.remove('alerte');
    if (lignes.length === 0) {
        zone.textContent = 'Sans répartition, la totalité du montant est classée « Non classé ».';
        return;
    }

    const reparti = lignes.reduce((somme, l) => somme + (Number(l.querySelector('input').value) || 0), 0);
    const reste = Math.round((total - reparti) * 100) / 100;
    if (reste >= 0) {
        zone.textContent =  '' + montant(reparti, code) + 'dépasse le montant de l\'opération ('
        + montant(total, code) + ').';
    } else {
        zone.textContent = 'Impossible, le montant est supérieur à '
            + montant(total, code) + '.';
        zone.classList.add('alerte');
    }
}

function ajouterLigneRepartition() {
    const ligne = document.createElement('div');
    ligne.className = 'ligne-repartition';

    const select = document.createElement('select');
    select.setAttribute('aria-label', 'Catégorie');
    remplirSelect(select, etat.categories, (c) => c.idCategorie, (c) => c.nom);

    const saisie = document.createElement('input');
    saisie.type = 'number';
    saisie.min = '0.01';
    saisie.step = '0.01';
    saisie.placeholder = 'Montant';
    saisie.setAttribute('aria-label', 'Montant affecté à la catégorie');
    saisie.inputMode = 'decimal';
    saisie.required = true;
    saisie.addEventListener('input', majResumeRepartition);

    ligne.appendChild(select);
    ligne.appendChild(saisie);
    ligne.appendChild(bouton('Retirer', 'bouton contour petit', () => {
        ligne.remove();
        majResumeRepartition();
    }, 'Retirer cette catégorie'));
    $('#lignes-repartition').appendChild(ligne);
    majResumeRepartition();
}

function majTypeSaisie() {
    const estRevenu = $('#form-saisie').type.value === 'REVENU';
    $('#bloc-source').hidden = !estRevenu;
    $('#bloc-repartition').hidden = estRevenu;
    $('#lignes-repartition').querySelectorAll('input').forEach((i) => { i.required = !estRevenu; });
}

async function enregistrerSaisie(evenement) {
    evenement.preventDefault();
    masquerMessage();
    const f = evenement.target;
    const envoi = f.querySelector('button[type="submit"]');
    const corps = {
        idCompte: Number(f.idCompte.value),
        nom: f.nom.value.trim(),
        description: f.description.value.trim() || null,
        montant: Number(f.montant.value),
        date: f.date.value
    };

    let url;
    if (f.type.value === 'REVENU') {
        url = '/api/revenus';
        corps.idSource = Number(f.idSource.value);
    } else {
        url = '/api/depenses';
        corps.repartitions = [...$('#lignes-repartition').children].map((ligne) => ({
            idCategorie: Number(ligne.querySelector('select').value),
            montant: Number(ligne.querySelector('input').value)
        }));
    }

    envoi.disabled = true;
    const resultat = await executer(() => api('POST', url, corps));
    envoi.disabled = false;

    if (resultat) {
        afficherMessage('Opération enregistrée. Nouveau solde : ' + montant(resultat.solde, devisesDuCompte(corps.idCompte)), 'succes');
        f.nom.value = '';
        f.description.value = '';
        f.montant.value = '';
        $('#lignes-repartition').replaceChildren();
        majResumeRepartition();
        etat.compteOuvert = corps.idCompte;
        await executer(chargerComptes);
    }
}

/* ------------------------------------------------------------------ */
/* Utilisateur : tableau de bord                                       */
/* ------------------------------------------------------------------ */

function dessinerFlux(zone, lignes, code, animer) {
    zone.replaceChildren();
    zone.classList.toggle('fixe', !animer);

    const styleZone = getComputedStyle(zone);
    const utile = zone.clientWidth - parseFloat(styleZone.paddingLeft) - parseFloat(styleZone.paddingRight);
    const n = lignes.length;
    const largeur = Math.max(utile, n * 56, 260);
    const colonne = largeur / n;
    const largeurBarre = Math.min(46, colonne * 0.52);
    const avecValeurs = colonne >= 56;

    // Même échelle pour les revenus (au-dessus de la ligne de base) et les dépenses (en dessous) ;
    // chaque moitié ne prend que la hauteur dont elle a besoin.
    const marge = 26;
    const hauteurMax = 108;
    const maxRevenus = Math.max(0, ...lignes.map((l) => l.revenus));
    const maxDepenses = Math.max(0, ...lignes.map((l) => l.depenses));
    const echelle = hauteurMax / Math.max(1, maxRevenus, maxDepenses);
    const hauteurHaut = Math.max(16, Math.ceil(maxRevenus * echelle));
    const hauteurBas = Math.max(16, Math.ceil(maxDepenses * echelle));
    const base = marge + hauteurHaut;
    const yMois = base + hauteurBas + 40;
    const yNet = yMois + 18;
    const hauteur = yNet + 10;

    const svg = noeudSvg('svg', {
        class: 'flux-svg',
        width: largeur,
        height: hauteur,
        viewBox: '0 0 ' + largeur + ' ' + hauteur,
        role: 'img',
        'aria-label': 'Revenus et dépenses par mois, en ' + code
    });
    svg.appendChild(noeudSvg('line', { class: 'base', x1: 0, x2: largeur, y1: base, y2: base }));

    lignes.forEach((l, i) => {
        const centre = (i + 0.5) * colonne;
        const difference = l.revenus - l.depenses;
        const groupe = noeudSvg('g', { class: 'mois' });

        const titre = noeudSvg('title');
        titre.textContent = moisFr(l.mois) + ' : revenus ' + montant(l.revenus, code)
            + ', dépenses ' + montant(l.depenses, code)
            + ', différence ' + montant(difference, code);
        groupe.appendChild(titre);

        const hr = l.revenus > 0 ? Math.min(hauteurHaut, Math.max(3, l.revenus * echelle)) : 0;
        const hd = l.depenses > 0 ? Math.min(hauteurBas, Math.max(3, l.depenses * echelle)) : 0;
        const rayon = Math.min(6, largeurBarre / 2);

        if (hr > 0) {
            const r = noeudSvg('rect', {
                class: 'barre-revenu', x: centre - largeurBarre / 2, y: base - 2 - hr,
                width: largeurBarre, height: hr, rx: rayon
            });
            r.style.animationDelay = (i * 45) + 'ms';
            groupe.appendChild(r);
            if (avecValeurs) {
                groupe.appendChild(texteSvg(montantCompact(l.revenus, code),
                    { x: centre, y: base - 2 - hr - 6, 'text-anchor': 'middle' }));
            }
        }
        if (hd > 0) {
            const r = noeudSvg('rect', {
                class: 'barre-depense', x: centre - largeurBarre / 2, y: base + 2,
                width: largeurBarre, height: hd, rx: rayon
            });
            r.style.animationDelay = (i * 45) + 'ms';
            groupe.appendChild(r);
            if (avecValeurs) {
                groupe.appendChild(texteSvg(montantCompact(l.depenses, code),
                    { x: centre, y: base + 2 + hd + 14, 'text-anchor': 'middle' }));
            }
        }

        groupe.appendChild(texteSvg(moisCourt(l.mois),
            { class: 'mois-libelle', x: centre, y: yMois, 'text-anchor': 'middle' }));
        if (avecValeurs) {
            groupe.appendChild(texteSvg((difference >= 0 ? '+' : MOINS) + montantCompact(Math.abs(difference), code),
                { class: difference >= 0 ? 'net-positif' : 'net-negatif', x: centre, y: yNet, 'text-anchor': 'middle' }));
        }
        svg.appendChild(groupe);
    });

    zone.appendChild(svg);
}

function blocFlux(code, lignes) {
    const bloc = document.createElement('article');
    bloc.className = 'feuille flux-devise';

    const entete = document.createElement('div');
    entete.className = 'flux-entete';
    const titre = document.createElement('h3');
    titre.textContent = 'En ' + code;
    const legende = document.createElement('ul');
    legende.className = 'legende';
    for (const [classe, texte] of [['l-revenu', 'Revenus'], ['l-depense', 'Dépenses']]) {
        const li = document.createElement('li');
        li.className = classe;
        li.textContent = texte;
        legende.appendChild(li);
    }
    entete.appendChild(titre);
    entete.appendChild(legende);
    bloc.appendChild(entete);

    const zone = document.createElement('div');
    zone.className = 'flux';
    bloc.appendChild(zone);

    const { table, tbody } = creerTable('registre fiche', [
        { titre: 'Mois' },
        { titre: 'Revenus', nombre: true },
        { titre: 'Dépenses', nombre: true },
        { titre: 'Différence', nombre: true }
    ]);
    const decroissant = [...lignes].sort((a, b) => b.mois.localeCompare(a.mois));
    for (const l of decroissant) {
        const difference = l.revenus - l.depenses;
        const tr = document.createElement('tr');
        tr.appendChild(cellule(moisFr(l.mois), 'c-titre'));
        tr.appendChild(cellule(montant(l.revenus, code), 'nombre revenu', 'Revenus'));
        tr.appendChild(cellule(montant(l.depenses, code), 'nombre depense', 'Dépenses'));
        tr.appendChild(cellule(montantSigne(difference, code, difference >= 0),
            'nombre c-valeur ' + (difference >= 0 ? 'revenu' : 'depense'), 'Différence'));
        tbody.appendChild(tr);
    }
    bloc.appendChild(table);

    return { bloc, zone };
}

// ---------- Répartition des dépenses par catégorie ----------

// Douze couleurs de catégorie (--cat-1 à --cat-12 dans style.css). Les catégories sont classées
// de la plus grosse part à la plus petite et reçoivent les couleurs dans cet ordre : deux blocs
// voisins portent toujours deux teintes voisines de la palette, validées pour être distinguables.
// Au-delà de la 12e (catégories ajoutées par l'administrateur), les plus petites sont regroupées
// dans un bloc gris (emplacement 0) ; leurs lignes restent dans le tableau.
const NB_COULEURS_CATEGORIES = 12;

function classeCategorie(emplacement) {
    return emplacement > 0 ? 'cat-' + emplacement : 'cat-autres';
}

function segmentBande(bloc, emplacement, part, titre) {
    const segment = document.createElement('span');
    segment.className = classeCategorie(emplacement);
    segment.dataset.bloc = bloc;
    segment.style.flexGrow = String(part);
    segment.title = titre;
    const etiquette = document.createElement('span');
    etiquette.className = 'etiquette';
    etiquette.textContent = pourcentage(part);
    segment.appendChild(etiquette);
    return segment;
}

// La part s'écrit dans le bloc seulement si elle y tient ; sinon le tableau la donne.
function ajusterEtiquettes(bande) {
    if (bande.clientWidth === 0) {
        return;
    }
    for (const segment of bande.children) {
        const etiquette = segment.firstElementChild;
        etiquette.hidden = false;
        const marges = 16;
        etiquette.hidden = segment.clientWidth < etiquette.offsetWidth + marges;
    }
}

function surveillerBande(bande) {
    if ('ResizeObserver' in window) {
        new ResizeObserver(() => ajusterEtiquettes(bande)).observe(bande);
    } else {
        ajusterEtiquettes(bande);
    }
    if (document.fonts && document.fonts.ready) {
        document.fonts.ready.then(() => ajusterEtiquettes(bande));
    }
}

// Survol : la catégorie visée ressort dans la bande et dans le tableau, le reste s'efface.
function lierSurvolRepartition(feuille) {
    const eteindre = () => {
        feuille.classList.remove('survol');
        feuille.querySelectorAll('.actif').forEach((e) => e.classList.remove('actif'));
    };
    feuille.addEventListener('pointerover', (evenement) => {
        const cible = evenement.target.closest('[data-bloc]');
        if (!cible || !feuille.contains(cible)) {
            eteindre();
            return;
        }
        const estLigne = cible.tagName === 'TR';
        feuille.classList.add('survol');
        feuille.querySelectorAll('[data-bloc]').forEach((e) => {
            const memeBloc = e.dataset.bloc === cible.dataset.bloc;
            // Depuis une ligne, seule cette ligne s'allume ; depuis un bloc, toutes ses lignes.
            e.classList.toggle('actif', e.tagName === 'TR' && estLigne ? e === cible : memeBloc);
        });
    });
    feuille.addEventListener('pointerleave', eteindre);
}

function afficherRepartition(categories) {
    const zone = $('#repartition');
    zone.replaceChildren();

    if (categories.length === 0) {
        const vide = document.createElement('p');
        vide.className = 'aide';
        vide.textContent = 'Aucune dépense classée sur la période.';
        zone.appendChild(vide);
        return;
    }

    // Du plus gros au plus petit, dans la bande comme dans le tableau ; la couleur suit ce rang.
    const lignes = [...categories]
        .sort((a, b) => Number(b.total) - Number(a.total) || a.categorie.localeCompare(b.categorie, 'fr'))
        .map((c, rang) => ({
            c,
            emplacement: rang < NB_COULEURS_CATEGORIES ? rang + 1 : 0,
            part: Math.max(Number(c.partPourcent), 0)
        }));
    const colorees = lignes.filter((l) => l.emplacement > 0);
    const regroupees = lignes.filter((l) => l.emplacement === 0);

    const feuille = document.createElement('div');
    feuille.className = 'feuille repartition';

    const bande = document.createElement('div');
    bande.className = 'bande';
    bande.setAttribute('role', 'img');

    const { table, tbody } = creerTable('registre fiche registre-categories', [
        { titre: 'Catégorie' },
        { titre: 'Dépenses', nombre: true },
        { titre: 'Total', nombre: true },
        { titre: 'Part', nombre: true }
    ]);

    const decrit = [];
    for (const l of colorees) {
        const resume = l.c.categorie + ' : ' + montant(l.c.total) + ', ' + pourcentage(l.part);
        bande.appendChild(segmentBande('cat-' + l.emplacement, l.emplacement, l.part, resume));
        decrit.push(resume);
    }
    if (regroupees.length > 0) {
        const partTotale = regroupees.reduce((somme, l) => somme + l.part, 0);
        const montantTotal = regroupees.reduce((somme, l) => somme + Number(l.c.total), 0);
        const nom = regroupees.length === 1 ? regroupees[0].c.categorie : 'Autres catégories';
        const detail = regroupees.length === 1
            ? ''
            : ' (' + regroupees.map((l) => l.c.categorie + ' ' + montant(l.c.total) + ', ' + pourcentage(l.part)).join(' ; ') + ')';
        const resume = nom + ' : ' + montant(montantTotal) + ', ' + pourcentage(partTotale) + detail;
        bande.appendChild(segmentBande('cat-autres', 0, partTotale, resume));
        decrit.push(resume);
    }
    bande.setAttribute('aria-label', 'Répartition des dépenses par catégorie. ' + decrit.join(' ; ') + '.');

    for (const l of lignes) {
        const tr = document.createElement('tr');
        tr.dataset.bloc = l.emplacement > 0 ? 'cat-' + l.emplacement : 'cat-autres';
        const nom = document.createElement('td');
        nom.className = 'c-titre';
        const teinte = document.createElement('span');
        teinte.className = 'teinte ' + classeCategorie(l.emplacement);
        nom.appendChild(teinte);
        nom.append(l.c.categorie);
        tr.appendChild(nom);
        const nb = cellule(String(l.c.nbDepenses), 'nombre c-nb', 'Dépenses');
        nb.dataset.unite = Number(l.c.nbDepenses) > 1 ? 'dépenses' : 'dépense';
        tr.appendChild(nb);
        tr.appendChild(cellule(montant(l.c.total), 'nombre c-valeur c-total', 'Total'));
        tr.appendChild(cellule(pourcentage(l.c.partPourcent), 'nombre c-part', 'Part'));
        tbody.appendChild(tr);
    }

    feuille.appendChild(bande);
    feuille.appendChild(table);
    zone.appendChild(feuille);
    lierSurvolRepartition(feuille);
    surveillerBande(bande);
}

async function chargerTableauBord() {
    const donnees = await executer(() => api('GET', '/api/tableau-de-bord?mois=' + etat.periode));
    if (!donnees) {
        return;
    }

    const parMois = new Map();
    let rafraichi = null;
    for (const l of donnees.synthese) {
        const cle = l.mois + '|' + l.devise;
        if (!parMois.has(cle)) {
            parMois.set(cle, { mois: l.mois, devise: l.devise, revenus: 0, depenses: 0 });
        }
        const ligne = parMois.get(cle);
        if (l.type === 'REVENU') {
            ligne.revenus += Number(l.total);
        } else {
            ligne.depenses += Number(l.total);
        }
        if (!rafraichi || l.rafraichiLe > rafraichi) {
            rafraichi = l.rafraichiLe;
        }
    }

    $('#tableau-rafraichi').textContent = rafraichi
        ? 'Données calculées le ' + dateFr(rafraichi) + ' à ' + String(rafraichi).substring(11, 16) + '.'
        : 'Aucune donnée calculée pour cette période.';

    const parDevise = new Map();
    for (const l of parMois.values()) {
        if (!parDevise.has(l.devise)) {
            parDevise.set(l.devise, []);
        }
        parDevise.get(l.devise).push(l);
    }

    const flux = $('#flux');
    flux.replaceChildren();
    etat.flux = [];

    if (parDevise.size === 0) {
        const vide = document.createElement('p');
        vide.className = 'aide';
        vide.textContent = 'Aucune opération sur la période. Si vous venez d\'en enregistrer, elles apparaîtront après le prochain rafraîchissement.';
        flux.appendChild(vide);
    }

    const codes = [...parDevise.keys()].sort((a, b) => a.localeCompare(b));
    for (const code of codes) {
        const lignes = parDevise.get(code).sort((a, b) => a.mois.localeCompare(b.mois));
        const { bloc, zone } = blocFlux(code, lignes);
        flux.appendChild(bloc);
        etat.flux.push({ zone, lignes, code });
        dessinerFlux(zone, lignes, code, true);
    }

    afficherRepartition(donnees.categories);
}

function changerPeriode(evenement) {
    const b = evenement.target.closest('button[data-mois]');
    if (!b) {
        return;
    }
    masquerMessage();
    etat.periode = Number(b.dataset.mois);
    $('#tableau-periode').querySelectorAll('button').forEach((x) => {
        x.setAttribute('aria-pressed', String(x === b));
    });
    chargerTableauBord();
}

/* ------------------------------------------------------------------ */
/* Administration                                                      */
/* ------------------------------------------------------------------ */

async function chargerUtilisateurs() {
    const utilisateurs = await executer(() => api('GET', '/api/admin/utilisateurs'));
    if (!utilisateurs) {
        return;
    }
    const tbody = $('#table-utilisateurs tbody');
    tbody.replaceChildren();
    if (utilisateurs.length === 0) {
        ligneVide(tbody, 5, 'Aucun utilisateur.');
    }
    for (const u of utilisateurs) {
        const tr = document.createElement('tr');
        tr.appendChild(cellule(u.pseudo, 'c-titre'));
        tr.appendChild(cellule(u.email, 'c-sous c-email'));

        const role = document.createElement('td');
        role.className = 'c-role';
        const pastille = document.createElement('span');
        pastille.className = 'pastille' + (u.role === 'ADMIN' ? ' admin' : '');
        pastille.textContent = u.role === 'ADMIN' ? 'Administrateur' : 'Utilisateur';
        role.appendChild(pastille);
        tr.appendChild(role);

        tr.appendChild(cellule(dateFr(u.dateInscription), 'c-sous c-date'));

        const actions = document.createElement('td');
        actions.className = 'nombre c-actions';
        if (u.idUtilisateur !== etat.session.idUtilisateur) {
            actions.appendChild(bouton('Supprimer', 'bouton danger petit', () => supprimerUtilisateur(u),
                'Supprimer l\'utilisateur ' + u.pseudo));
        } else {
            const vous = document.createElement('span');
            vous.className = 'vous';
            vous.textContent = 'Vous';
            actions.appendChild(vous);
        }
        tr.appendChild(actions);
        tbody.appendChild(tr);
    }
}

async function creerUtilisateur(evenement) {
    evenement.preventDefault();
    masquerMessage();
    const f = evenement.target;
    const corps = {
        role: f.role.value,
        pseudo: f.pseudo.value.trim(),
        email: f.email.value.trim(),
        motDePasse: f.motDePasse.value
    };
    const ok = await executer(() => api('POST', '/api/admin/utilisateurs', corps), 'Utilisateur créé.');
    if (ok !== undefined) {
        f.reset();
        await chargerUtilisateurs();
    }
}

async function supprimerUtilisateur(u) {
    if (!await confirmer('Supprimer l\'utilisateur « ' + u.pseudo + ' » ?', 'Cette action est définitive.')) {
        return;
    }
    masquerMessage();
    const ok = await executer(() => api('DELETE', '/api/admin/utilisateurs/' + u.idUtilisateur), 'Utilisateur supprimé.');
    if (ok !== undefined) {
        await chargerUtilisateurs();
    }
}

async function chargerStatistiques() {
    const stats = await executer(() => api('GET', '/api/admin/statistiques'));
    if (!stats) {
        return;
    }
    const tbody = $('#table-statistiques tbody');
    tbody.replaceChildren();
    if (stats.length === 0) {
        ligneVide(tbody, 5, 'Aucune statistique pour le moment.');
    }
    for (const s of stats) {
        const tr = document.createElement('tr');
        tr.appendChild(cellule(moisFr(s.mois), 'c-titre'));
        tr.appendChild(cellule(String(s.utilisateursActifs), 'nombre', 'Utilisateurs actifs'));
        tr.appendChild(cellule(String(s.nbTransactions), 'nombre', 'Transactions'));
        tr.appendChild(cellule(String(s.nbRevenus), 'nombre', 'Revenus'));
        tr.appendChild(cellule(String(s.nbDepenses), 'nombre', 'Dépenses'));
        tbody.appendChild(tr);
    }
}

async function rafraichirTableauBord() {
    const b = $('#bouton-rafraichir');
    const libelle = b.textContent;
    masquerMessage();
    b.disabled = true;
    b.textContent = 'Rafraîchissement…';
    await executer(() => api('POST', '/api/admin/rafraichir'), 'Tableau de bord rafraîchi pour tous les utilisateurs.');
    b.disabled = false;
    b.textContent = libelle;
}

async function chargerCategories() {
    etat.categories = await api('GET', '/api/categories');
    const liste = $('#liste-categories');
    liste.replaceChildren();
    for (const c of etat.categories) {
        const li = document.createElement('li');
        li.textContent = c.nom;
        liste.appendChild(li);
    }
}

async function creerCategorie(evenement) {
    evenement.preventDefault();
    masquerMessage();
    const f = evenement.target;
    const ok = await executer(() => api('POST', '/api/admin/categories', { nom: f.nom.value.trim() }), 'Catégorie ajoutée.');
    if (ok !== undefined) {
        f.reset();
        await executer(chargerCategories);
    }
}

const chargements = {
    'onglet-comptes': () => executer(chargerComptes),
    'onglet-saisie': () => executer(chargerComptes),
    'onglet-tableau': chargerTableauBord,
    'onglet-utilisateurs': chargerUtilisateurs,
    'onglet-statistiques': chargerStatistiques,
    'onglet-categories': () => executer(chargerCategories)
};

/* ------------------------------------------------------------------ */
/* Session                                                             */
/* ------------------------------------------------------------------ */

async function connecter(evenement) {
    evenement.preventDefault();
    masquerMessage();
    const f = evenement.target;
    const session = await executer(() => api('POST', '/api/connexion', {
        email: f.email.value.trim(),
        motDePasse: f.motDePasse.value
    }), undefined, 'Connexion refusée');
    if (session) {
        etat.session = session;
        f.reset();
        masquerMessage();
        await executer(afficherApplication);
    }
}

async function deconnecter() {
    await executer(() => api('POST', '/api/deconnexion'));
    etat.session = null;
    etat.compteOuvert = null;
    etat.comptes = [];
    $('#bloc-transactions').hidden = true;
    $('#lignes-repartition').replaceChildren();
    masquerMessage();
    afficherConnexion();
}

function preRemplirConnexion(evenement) {
    const puce = evenement.target.closest('.puce');
    if (!puce) {
        return;
    }
    const f = $('#form-connexion');
    f.email.value = puce.dataset.email;
    f.motDePasse.value = puce.dataset.mdp;
    f.querySelector('button[type="submit"]').focus();
}

function dateDuJour() {
    const d = new Date();
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
}

async function demarrer() {
    initialiserTheme();
    activerOnglets($('#nav-utilisateur'), $('#ecran-utilisateur'));
    activerOnglets($('#nav-admin'), $('#ecran-admin'));

    $('#form-connexion').addEventListener('submit', connecter);
    $('.comptes-test').addEventListener('click', preRemplirConnexion);
    $('#bouton-deconnexion').addEventListener('click', deconnecter);
    $('#message-fermer').addEventListener('click', masquerMessage);

    $('#form-saisie').addEventListener('submit', enregistrerSaisie);
    $('#form-saisie').addEventListener('change', (e) => { if (e.target.name === 'type') majTypeSaisie(); });
    $('#form-saisie').addEventListener('input', (e) => { if (e.target.name === 'montant') majResumeRepartition(); });
    $('#saisie-compte').addEventListener('change', majSuffixeDevise);
    $('#bouton-ajout-repartition').addEventListener('click', ajouterLigneRepartition);
    $('#form-compte').addEventListener('submit', creerCompte);
    $('#tableau-periode').addEventListener('click', changerPeriode);
    $('#form-utilisateur').addEventListener('submit', creerUtilisateur);
    $('#bouton-rafraichir').addEventListener('click', rafraichirTableauBord);
    $('#form-categorie').addEventListener('submit', creerCategorie);

    const dialogue = $('#dialogue-confirmation');
    dialogue.addEventListener('click', (e) => { if (e.target === dialogue) dialogue.close('annuler'); });

    let minuteur = null;
    let largeurPrecedente = window.innerWidth;
    window.addEventListener('resize', () => {
        clearTimeout(minuteur);
        minuteur = setTimeout(() => {
            if (window.innerWidth === largeurPrecedente) {
                return;
            }
            largeurPrecedente = window.innerWidth;
            etat.flux.forEach((f) => dessinerFlux(f.zone, f.lignes, f.code, false));
        }, 150);
    });

    $('#saisie-date').value = dateDuJour();
    majResumeRepartition();

    try {
        etat.session = await api('GET', '/api/session');
    } catch (e) {
        etat.session = null;
    }

    if (etat.session) {
        await executer(afficherApplication);
    } else {
        afficherConnexion();
    }
}

demarrer();